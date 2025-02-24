package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.wallet.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.stellar.sdk.responses.TransactionResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorDetectionService @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val walletManager: WalletManager,
    private val auditLogger: SecureAuditLogger
) {
    private val _detectedErrors = MutableStateFlow<Map<String, TransactionError>>(emptyMap())
    val detectedErrors: StateFlow<Map<String, TransactionError>> = _detectedErrors.asStateFlow()

    suspend fun analyzeTransaction(transactionId: String): TransactionError? {
        return try {
            val transaction = stellarTransactionManager.getTransaction(transactionId)
            when {
                !walletManager.isConnected() -> {
                    createWalletConnectionError(transactionId)
                }
                isNetworkCongested(transaction) -> {
                    createNetworkCongestionError(transactionId, transaction)
                }
                isInsufficientFunds(transaction) -> {
                    createInsufficientFundsError(transactionId, transaction)
                }
                isSmartContractError(transaction) -> {
                    createSmartContractError(transactionId, transaction)
                }
                isEscrowError(transaction) -> {
                    createEscrowError(transactionId, transaction)
                }
                else -> null
            }?.also { error ->
                _detectedErrors.value = _detectedErrors.value + (transactionId to error)
                auditLogger.logEvent(
                    "ERROR_DETECTED",
                    "Detected error for transaction: $transactionId",
                    mapOf(
                        "error_type" to error::class.simpleName,
                        "error_message" to error.message,
                        "severity" to error.severity.name
                    )
                )
            }
        } catch (e: Exception) {
            UnknownError(
                transactionId = transactionId,
                message = "Failed to analyze transaction: ${e.message}",
                severity = ErrorSeverity.HIGH
            ).also { error ->
                _detectedErrors.value = _detectedErrors.value + (transactionId to error)
                auditLogger.logEvent(
                    "ERROR_DETECTION_FAILED",
                    "Failed to analyze transaction: $transactionId",
                    mapOf("error" to e.message)
                )
            }
        }
    }

    private fun isNetworkCongested(transaction: TransactionResponse): Boolean {
        return transaction.let {
            val latency = it.ledger.closedAt.time - it.createdAt.time
            val hasTimeouts = it.resultXdr.contains("tx_too_late")
            val hasFailedOperations = it.resultXdr.contains("op_failed")
            
            latency > 10000 || // More than 10 seconds latency
            hasTimeouts ||
            hasFailedOperations
        }
    }

    private fun isInsufficientFunds(transaction: TransactionResponse): Boolean {
        return transaction.let {
            val resultCode = it.resultXdr
            resultCode.contains("tx_insufficient_balance") ||
            resultCode.contains("op_underfunded") ||
            resultCode.contains("op_low_reserve")
        }
    }

    private fun isSmartContractError(transaction: TransactionResponse): Boolean {
        return transaction.let {
            val resultCode = it.resultXdr
            resultCode.contains("op_invalid_code") ||
            resultCode.contains("op_execution_failed") ||
            resultCode.contains("op_exceeds_limit")
        }
    }

    private fun isEscrowError(transaction: TransactionResponse): Boolean {
        return transaction.let {
            val resultCode = it.resultXdr
            val operations = it.operations
            
            // Check if this is an escrow operation
            val isEscrowOp = operations.any { op ->
                op.type == "manage_escrow"
            }
            
            isEscrowOp && (
                resultCode.contains("escrow_invalid_state") ||
                resultCode.contains("escrow_not_found") ||
                resultCode.contains("escrow_unauthorized")
            )
        }
    }

    private fun createWalletConnectionError(transactionId: String): WalletConnectionError {
        return WalletConnectionError(
            transactionId = transactionId,
            message = "Wallet connection lost during transaction",
            lastConnectedTimestamp = System.currentTimeMillis(),
            connectionAttempts = 1,
            walletType = "Stellar"
        )
    }

    private fun createNetworkCongestionError(
        transactionId: String,
        transaction: TransactionResponse
    ): NetworkCongestionError {
        val latency = transaction.ledger.closedAt.time - transaction.createdAt.time
        val retryAfter = when {
            latency > 30000 -> 60000L // 1 minute for severe congestion
            latency > 20000 -> 30000L // 30 seconds for high congestion
            else -> 15000L // 15 seconds for moderate congestion
        }
        
        val congestionLevel = when {
            latency > 30000 -> CongestionLevel.EXTREME
            latency > 20000 -> CongestionLevel.HIGH
            latency > 10000 -> CongestionLevel.MEDIUM
            else -> CongestionLevel.LOW
        }

        return NetworkCongestionError(
            transactionId = transactionId,
            message = "Network congestion detected. Retry after ${retryAfter/1000} seconds",
            retryAfter = System.currentTimeMillis() + retryAfter,
            networkLatency = latency,
            congestionLevel = congestionLevel
        )
    }

    private fun createInsufficientFundsError(
        transactionId: String,
        transaction: TransactionResponse
    ): InsufficientFundsError {
        val sourceAccount = transaction.sourceAccount
        val requiredAmount = transaction.feeCharged
        val availableAmount = sourceAccount.balances.firstOrNull { 
            it.assetType == "native" 
        }?.balance ?: "0"

        return InsufficientFundsError(
            transactionId = transactionId,
            message = "Insufficient funds for transaction. Required: $requiredAmount XLM, Available: $availableAmount XLM",
            requiredAmount = requiredAmount.toString(),
            availableAmount = availableAmount,
            currency = "XLM"
        )
    }

    private fun createSmartContractError(
        transactionId: String,
        transaction: TransactionResponse
    ): SmartContractError {
        val operation = transaction.operations.firstOrNull { 
            it.type == "invoke_contract" 
        }
        
        return SmartContractError(
            transactionId = transactionId,
            message = "Smart contract execution failed: ${transaction.resultXdr}",
            contractAddress = operation?.sourceAccount ?: "unknown",
            errorCode = transaction.resultXdr,
            functionName = operation?.let { 
                it.body.asJsonObject["function_name"]?.asString 
            },
            parameters = operation?.let {
                it.body.asJsonObject["parameters"]?.asJsonObject?.entrySet()?.associate { entry ->
                    entry.key to entry.value.asString
                }
            } ?: emptyMap()
        )
    }

    private fun createEscrowError(
        transactionId: String,
        transaction: TransactionResponse
    ): EscrowError {
        val escrowOp = transaction.operations.first { 
            it.type == "manage_escrow" 
        }
        
        return EscrowError(
            transactionId = transactionId,
            message = "Escrow verification failed: ${transaction.resultXdr}",
            escrowContractAddress = escrowOp.sourceAccount,
            escrowState = transaction.resultXdr,
            participantAddresses = transaction.operations
                .filter { it.type == "manage_escrow" }
                .map { it.sourceAccount }
        )
    }

    fun clearError(transactionId: String) {
        _detectedErrors.value = _detectedErrors.value - transactionId
        auditLogger.logEvent(
            "ERROR_CLEARED",
            "Cleared error for transaction: $transactionId",
            mapOf("timestamp" to System.currentTimeMillis())
        )
    }
} 