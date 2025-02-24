package com.example.fideicomisoapproverring.recovery.forensics

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.stellar.model.StellarTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Service responsible for transaction forensics analysis, error pattern detection,
 * and maintaining detailed audit trails for the Revolutionary Farmers marketplace.
 */
@Singleton
class TransactionForensicsService @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val recoveryService: TransactionRecoveryService,
    private val auditLogger: SecureAuditLogger,
    private val blockchainService: BlockchainService,
    private val walletService: WalletService,
    private val escrowService: EscrowService
) {
    private val forensicsMutex = Mutex()
    private val errorPatterns = mutableMapOf<String, List<TransactionError>>()
    
    private val _forensicsState = MutableStateFlow<Map<String, ForensicsReport>>(emptyMap())
    val forensicsState: StateFlow<Map<String, ForensicsReport>> = _forensicsState.asStateFlow()

    private val errorPatternCache = mutableMapOf<String, List<ErrorPattern>>()
    
    /**
     * Analyzes a transaction error and generates a detailed forensics report
     */
    suspend fun analyzeError(transactionId: String, error: TransactionError) {
        forensicsMutex.withLock {
            val report = generateForensicsReport(transactionId, error)
            updateErrorPatterns(error)
            _forensicsState.value = _forensicsState.value + (transactionId to report)
            
            auditLogger.logEvent(
                "FORENSICS_ANALYSIS_COMPLETED",
                "Completed forensics analysis for transaction: $transactionId",
                mapOf(
                    "error_type" to (error::class.simpleName ?: "Unknown"),
                    "severity" to error.severity.name,
                    "timestamp" to Instant.now().toString()
                )
            )
        }
    }

    /**
     * Generates a detailed forensics report for a transaction error
     */
    private suspend fun generateForensicsReport(
        transactionId: String, 
        error: TransactionError
    ): ForensicsReport {
        val transaction = stellarTransactionManager.getTransaction(transactionId)
        val stateHistory = recoveryService.getTransactionStateHistory(transactionId)
        
        return ForensicsReport(
            transactionId = transactionId,
            timestamp = Instant.now(),
            errorDetails = ErrorDetails(
                type = error::class.simpleName ?: "Unknown",
                message = error.message,
                severity = error.severity,
                recoverable = error.recoverable
            ),
            transactionDetails = transaction.toTransactionDetails(),
            stateHistory = stateHistory,
            recommendations = generateRecommendations(error)
        )
    }

    /**
     * Converts a Stellar transaction to transaction details
     */
    private fun StellarTransaction.toTransactionDetails(): TransactionDetails {
        return TransactionDetails(
            createdAt = this.createdAt,
            lastModified = this.lastModified,
            operations = this.operations.map { it.type },
            participants = this.operations.map { it.sourceAccount }.distinct()
        )
    }

    /**
     * Updates error patterns for trend analysis
     */
    private fun updateErrorPatterns(error: TransactionError) {
        val errorType = error::class.simpleName ?: return
        val currentPatterns = errorPatterns.getOrDefault(errorType, emptyList())
        errorPatterns[errorType] = currentPatterns + error
        
        if (currentPatterns.size >= ERROR_PATTERN_THRESHOLD) {
            analyzeErrorPatterns(error)
        }
    }

    /**
     * Analyzes error patterns to detect trends and systemic issues
     */
    private suspend fun analyzeErrorPatterns(error: TransactionError) {
        val patterns = errorPatternCache[error.transactionId] ?: listOf(
            ErrorPattern(
                patternId = generatePatternId(error),
                description = generatePatternDescription(error),
                frequency = 1,
                firstOccurrence = error.timestamp,
                lastOccurrence = error.timestamp,
                relatedTransactions = listOf(error.transactionId)
            )
        )
        errorPatternCache[error.transactionId] = patterns
        
        val timeBasedPatterns = patterns
            .groupBy { 
                error -> error.timestamp.atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .mapValues { it.value.size }

        val significantIncrease = detectSignificantIncrease(timeBasedPatterns)
        if (significantIncrease) {
            auditLogger.logEvent(
                "ERROR_PATTERN_DETECTED",
                "Detected significant increase in error frequency",
                mapOf(
                    "error_type" to error::class.simpleName,
                    "pattern_size" to patterns.size.toString(),
                    "timestamp" to Instant.now().toString()
                )
            )
        }
    }

    /**
     * Generates recovery recommendations based on error analysis
     */
    private fun generateRecommendations(error: TransactionError): List<String> {
        return when (error) {
            is NetworkCongestionError -> listOf(
                "Wait for network congestion to decrease",
                "Retry transaction with higher fee",
                "Consider breaking down into smaller transactions"
            )
            is SmartContractError -> listOf(
                "Verify contract state preconditions",
                "Check parameter validation",
                "Review contract execution logs"
            )
            is EscrowError -> listOf(
                "Verify escrow account balances",
                "Check participant signatures",
                "Review escrow release conditions"
            )
            else -> listOf(
                "Verify transaction parameters",
                "Check account permissions",
                "Review system logs for additional context"
            )
        }
    }

    suspend fun analyzeTransaction(transactionId: String): ForensicsReport {
        auditLogger.logEvent(
            "FORENSICS_ANALYSIS_START",
            "Starting forensic analysis for transaction: $transactionId",
            mapOf("timestamp" to Instant.now().toString())
        )
        
        val error = getTransactionError(transactionId)
        val systemState = getCurrentSystemState()
        val errorPatterns = analyzeErrorPatterns(error)
        
        val isRecoverable = determineRecoverability(error, systemState)
        val requiresUserAction = determineUserActionRequired(error, systemState)
        val recommendedAction = generateRecommendedAction(error, systemState)
        val recoveryProbability = calculateRecoveryProbability(error, systemState, errorPatterns)
        
        return ForensicsReport(
            transactionId = transactionId,
            timestamp = Instant.now(),
            error = error,
            isRecoverable = isRecoverable,
            requiresUserAction = requiresUserAction,
            recommendedAction = recommendedAction,
            errorPatterns = errorPatterns,
            systemState = systemState,
            recoveryProbability = recoveryProbability
        ).also {
            auditLogger.logEvent(
                "FORENSICS_ANALYSIS_COMPLETE",
                "Completed forensic analysis for transaction: $transactionId",
                mapOf(
                    "timestamp" to it.timestamp.toString(),
                    "is_recoverable" to it.isRecoverable.toString(),
                    "requires_user_action" to it.requiresUserAction.toString(),
                    "recovery_probability" to it.recoveryProbability.toString()
                )
            )
        }
    }
    
    fun monitorSystemHealth(): Flow<SystemState> = flow {
        while (true) {
            val systemState = getCurrentSystemState()
            emit(systemState)
            kotlinx.coroutines.delay(SYSTEM_HEALTH_CHECK_INTERVAL)
        }
    }
    
    private suspend fun getCurrentSystemState(): SystemState {
        return SystemState(
            networkStatus = determineNetworkStatus(),
            walletStatus = walletService.getWalletStatus(),
            escrowStatus = escrowService.getEscrowStatus(),
            blockchainState = getBlockchainState()
        )
    }
    
    private suspend fun getBlockchainState(): BlockchainState {
        return blockchainService.getBlockchainState()
    }
    
    private suspend fun determineNetworkStatus(): NetworkStatus {
        return try {
            val state = blockchainService.getNetworkState()
            when {
                state.congestion > HIGH_CONGESTION_THRESHOLD -> NetworkStatus.CONGESTED
                state.congestion > MEDIUM_CONGESTION_THRESHOLD -> NetworkStatus.DEGRADED
                else -> NetworkStatus.HEALTHY
            }
        } catch (e: Exception) {
            NetworkStatus.OFFLINE
        }
    }
    
    private fun generatePatternId(error: TransactionError): String {
        return "${error::class.simpleName}_${error.severity}_${error.timestamp.toEpochMilli()}"
    }
    
    private fun generatePatternDescription(error: TransactionError): String {
        return when (error) {
            is TransactionError.NetworkCongestionError -> "Network congestion causing transaction delays"
            is TransactionError.WalletConnectionError -> "Wallet connection issues"
            is TransactionError.EscrowError -> "Escrow contract verification failed"
            is TransactionError.SmartContractError -> "Smart contract execution error"
            is TransactionError.InsufficientFundsError -> "Insufficient funds for transaction"
            is TransactionError.MaxRetriesExceeded -> "Maximum retry attempts reached"
            is TransactionError.UnknownError -> "Unknown transaction error"
        }
    }
    
    private suspend fun getTransactionError(transactionId: String): TransactionError {
        return blockchainService.getTransactionError(transactionId)
            ?: TransactionError.UnknownError(
                transactionId = transactionId,
                message = "No error information available",
                timestamp = Instant.now()
            )
    }
    
    private fun determineRecoverability(error: TransactionError, systemState: SystemState): Boolean {
        return when {
            error.severity == ErrorSeverity.CRITICAL -> false
            systemState.networkStatus == NetworkStatus.OFFLINE -> false
            systemState.walletStatus == WalletStatus.ERROR -> false
            systemState.escrowStatus == EscrowStatus.ERROR -> false
            else -> error.isRecoverable
        }
    }
    
    private fun determineUserActionRequired(error: TransactionError, systemState: SystemState): Boolean {
        return when {
            error is TransactionError.WalletConnectionError -> true
            error is TransactionError.EscrowError -> true
            systemState.walletStatus == WalletStatus.DISCONNECTED -> true
            systemState.escrowStatus == EscrowStatus.LOCKED -> true
            error.severity == ErrorSeverity.HIGH -> true
            else -> false
        }
    }
    
    private fun generateRecommendedAction(error: TransactionError, systemState: SystemState): String {
        return when {
            error is TransactionError.WalletConnectionError -> "Please verify your wallet connection"
            error is TransactionError.EscrowError -> "Verify escrow contract status"
            systemState.networkStatus == NetworkStatus.OFFLINE -> "Check your internet connection"
            systemState.walletStatus == WalletStatus.DISCONNECTED -> "Reconnect your wallet"
            error.severity == ErrorSeverity.CRITICAL -> "Contact support for assistance"
            else -> "Retry the transaction"
        }
    }
    
    private suspend fun calculateRecoveryProbability(
        error: TransactionError,
        systemState: SystemState,
        patterns: List<ErrorPattern>
    ): Float {
        var probability = 1.0f
        
        // Reduce probability based on error severity
        probability *= when (error.severity) {
            ErrorSeverity.LOW -> 0.9f
            ErrorSeverity.MEDIUM -> 0.7f
            ErrorSeverity.HIGH -> 0.4f
            ErrorSeverity.CRITICAL -> 0.1f
        }
        
        // Adjust based on system state
        if (systemState.networkStatus != NetworkStatus.HEALTHY) probability *= 0.8f
        if (systemState.walletStatus != WalletStatus.CONNECTED) probability *= 0.7f
        if (systemState.escrowStatus != EscrowStatus.ACTIVE) probability *= 0.6f
        
        // Consider error patterns
        if (patterns.isNotEmpty()) {
            val patternFactor = 1.0f - (patterns.size * 0.1f).coerceAtMost(0.5f)
            probability *= patternFactor
        }
        
        return probability.coerceIn(0.0f, 1.0f)
    }

    companion object {
        private const val ERROR_PATTERN_THRESHOLD = 5
        private const val SYSTEM_HEALTH_CHECK_INTERVAL = 30_000L // 30 seconds
        private const val HIGH_CONGESTION_THRESHOLD = 0.8f
        private const val MEDIUM_CONGESTION_THRESHOLD = 0.5f
    }
}

/**
 * Represents a detailed forensics report for a transaction error
 */
data class ForensicsReport(
    val transactionId: String,
    val timestamp: Instant,
    val errorDetails: ErrorDetails,
    val transactionDetails: TransactionDetails,
    val stateHistory: List<TransactionState>,
    val recommendations: List<String>
)

data class ErrorDetails(
    val type: String,
    val message: String,
    val severity: ErrorSeverity,
    val recoverable: Boolean
)

data class TransactionDetails(
    val createdAt: Instant,
    val lastModified: Instant,
    val operations: List<String>,
    val participants: List<String>
) 