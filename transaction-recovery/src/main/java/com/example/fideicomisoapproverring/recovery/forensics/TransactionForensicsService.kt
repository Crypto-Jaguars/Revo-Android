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

/**
 * Service responsible for transaction forensics analysis, error pattern detection,
 * and maintaining detailed audit trails for the Revolutionary Farmers marketplace.
 */
@Singleton
class TransactionForensicsService @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val recoveryService: TransactionRecoveryService,
    private val auditLogger: SecureAuditLogger
) {
    private val forensicsMutex = Mutex()
    private val errorPatterns = mutableMapOf<String, List<TransactionError>>()
    
    private val _forensicsState = MutableStateFlow<Map<String, ForensicsReport>>(emptyMap())
    val forensicsState: StateFlow<Map<String, ForensicsReport>> = _forensicsState.asStateFlow()

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
            analyzeErrorPatterns(errorType, currentPatterns)
        }
    }

    /**
     * Analyzes error patterns to detect trends and systemic issues
     */
    private fun analyzeErrorPatterns(errorType: String, patterns: List<TransactionError>) {
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
                    "error_type" to errorType,
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

    companion object {
        private const val ERROR_PATTERN_THRESHOLD = 5
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