package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.core.model.TransactionStatus
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.core.wallet.WalletManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class TransactionMonitorService @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val walletManager: WalletManager,
    private val auditLogger: SecureAuditLogger,
    private val recoveryService: TransactionRecoveryService,
    private val errorDetectionService: ErrorDetectionService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    companion object {
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val MAX_BACKOFF_MS = 30000L
        private const val MAX_RETRIES = 3
    }

    private val _monitoredTransactions = MutableStateFlow<Map<String, TransactionStatus>>(emptyMap())
    val monitoredTransactions: StateFlow<Map<String, TransactionStatus>> = _monitoredTransactions.asStateFlow()

    private val monitoringJobs = mutableMapOf<String, Job>()
    private val retryAttempts = mutableMapOf<String, Int>()

    fun startMonitoring(transactionId: String) {
        if (monitoringJobs.containsKey(transactionId)) {
            auditLogger.logEvent(
                "MONITOR_DUPLICATE",
                "Attempted to monitor already monitored transaction: $transactionId"
            )
            return
        }

        val job = scope.launch {
            try {
                monitorTransaction(transactionId)
            } catch (e: Exception) {
                handleMonitoringError(transactionId, e)
            }
        }

        monitoringJobs[transactionId] = job
        _monitoredTransactions.value = _monitoredTransactions.value + (transactionId to TransactionStatus.PENDING)
        
        auditLogger.logEvent(
            "MONITOR_START",
            "Started monitoring transaction: $transactionId"
        )
    }

    fun stopMonitoring(transactionId: String) {
        monitoringJobs[transactionId]?.cancel()
        monitoringJobs.remove(transactionId)
        retryAttempts.remove(transactionId)
        _monitoredTransactions.value = _monitoredTransactions.value - transactionId

        auditLogger.logEvent(
            "MONITOR_STOP",
            "Stopped monitoring transaction: $transactionId"
        )
    }

    private suspend fun monitorTransaction(transactionId: String) {
        var retryCount = retryAttempts.getOrDefault(transactionId, 0)

        while (isActive && retryCount < MAX_RETRIES) {
            try {
                val transaction = stellarTransactionManager.getTransaction(transactionId)
                
                // Use ErrorDetectionService to analyze the transaction
                val detectedError = errorDetectionService.analyzeTransaction(transactionId)
                
                if (detectedError != null) {
                    handleDetectedError(transactionId, detectedError)
                    break
                }
                
                if (transaction.isSuccessful()) {
                    handleSuccessfulTransaction(transactionId)
                    break
                } else {
                    handleFailedTransaction(transactionId, transaction)
                    break
                }
            } catch (e: Exception) {
                when (e) {
                    is SocketTimeoutException -> handleNetworkError(transactionId, e, retryCount)
                    else -> handleUnexpectedError(transactionId, e)
                }
                
                retryCount++
                retryAttempts[transactionId] = retryCount
                
                if (retryCount < MAX_RETRIES) {
                    delay(calculateBackoffDelay(retryCount))
                } else {
                    handleMaxRetriesReached(transactionId)
                    break
                }
            }
        }
    }

    private suspend fun handleSuccessfulTransaction(transactionId: String) {
        _monitoredTransactions.value = _monitoredTransactions.value + (transactionId to TransactionStatus.COMPLETED)
        
        auditLogger.logEvent(
            "TRANSACTION_SUCCESS",
            "Transaction completed successfully: $transactionId"
        )
        
        stopMonitoring(transactionId)
    }

    private suspend fun handleFailedTransaction(transactionId: String, transaction: Any) {
        val error = BlockchainError(
            transactionId = transactionId,
            message = "Transaction failed on blockchain",
            blockHeight = null,
            gasPrice = null,
            nonce = null
        )
        
        auditLogger.logEvent(
            "TRANSACTION_FAILED",
            "Transaction failed: $transactionId"
        )
        
        recoveryService.reportError(error)
        stopMonitoring(transactionId)
    }

    private suspend fun handleNetworkError(transactionId: String, error: SocketTimeoutException, retryCount: Int) {
        val networkError = NetworkCongestionError(
            transactionId = transactionId,
            message = "Network timeout occurred",
            retryAfter = calculateBackoffDelay(retryCount),
            congestionLevel = CongestionLevel.HIGH
        )
        
        auditLogger.logEvent(
            "NETWORK_ERROR",
            "Network error occurred: $transactionId",
            mapOf(
                "retry_count" to retryCount,
                "retry_after" to calculateBackoffDelay(retryCount)
            )
        )
        
        recoveryService.reportError(networkError)
    }

    private suspend fun handleUnexpectedError(transactionId: String, error: Exception) {
        val unexpectedError = UnknownError(
            transactionId = transactionId,
            message = "Unexpected error occurred: ${error.message}",
            stackTrace = error.stackTraceToString()
        )
        
        auditLogger.logEvent(
            "UNEXPECTED_ERROR",
            "Unexpected error occurred: $transactionId",
            mapOf("error_type" to error.javaClass.simpleName)
        )
        
        recoveryService.reportError(unexpectedError)
    }

    private suspend fun handleMaxRetriesReached(transactionId: String) {
        val error = TimeoutError(
            transactionId = transactionId,
            message = "Max retries reached for transaction",
            timeoutDuration = MAX_RETRIES * MAX_BACKOFF_MS,
            operationType = "TRANSACTION_MONITORING"
        )
        
        auditLogger.logEvent(
            "MAX_RETRIES_REACHED",
            "Maximum retry attempts reached: $transactionId",
            mapOf("max_retries" to MAX_RETRIES)
        )
        
        recoveryService.reportError(error)
        stopMonitoring(transactionId)
    }

    private fun calculateBackoffDelay(retryCount: Int): Long {
        return minOf(
            INITIAL_BACKOFF_MS * 2.0.pow(retryCount).toLong(),
            MAX_BACKOFF_MS
        )
    }

    private suspend fun handleDetectedError(transactionId: String, error: TransactionError) {
        auditLogger.logEvent(
            "ERROR_DETECTED",
            "Detected error during monitoring: $transactionId",
            mapOf(
                "error_type" to error::class.simpleName,
                "error_message" to error.message
            )
        )
        
        recoveryService.reportError(error)
        
        // Stop monitoring if error is not recoverable
        if (!error.recoverable) {
            stopMonitoring(transactionId)
        }
    }
} 