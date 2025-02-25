package com.example.fideicomisoapproverring.recovery.forensics

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.core.model.TransactionError
import com.example.fideicomisoapproverring.core.model.ErrorSeverity
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.service.BlockchainService
import com.example.fideicomisoapproverring.recovery.service.EscrowService
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import com.example.fideicomisoapproverring.recovery.service.WalletService
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
 * Service responsible for analyzing transaction errors and generating forensics reports
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
    private val transactionAuditTrails = mutableMapOf<String, MutableList<AuditTrailEntry>>()
    
    private val _forensicsReports = MutableStateFlow<Map<String, ForensicsReport>>(emptyMap())
    val forensicsReports: StateFlow<Map<String, ForensicsReport>> = _forensicsReports.asStateFlow()

    private val errorPatternCache = mutableMapOf<String, List<ErrorPattern>>()
    private val realtimeAnalytics = MutableStateFlow<RealtimeAnalytics>(RealtimeAnalytics())
    private val predictiveModel = mutableMapOf<String, ErrorPrediction>()
    
    data class RealtimeAnalytics(
        val activeTransactions: Int = 0,
        val errorRate: Float = 0f,
        val avgResponseTime: Long = 0L,
        val networkHealth: Float = 1f,
        val timestamp: Instant = Instant.now()
    )
    
    data class ErrorPrediction(
        val transactionId: String,
        val predictedErrorType: String,
        val probability: Float,
        val factors: List<String>,
        val timestamp: Instant
    )

    /**
     * Records a new audit trail entry for a transaction
     */
    private suspend fun recordAuditTrail(
        transactionId: String,
        action: String,
        details: String,
        metadata: Map<String, String> = emptyMap(),
        severity: AuditSeverity = AuditSeverity.INFO
    ) {
        forensicsMutex.withLock {
            val entry = AuditTrailEntry(
                timestamp = Instant.now(),
                action = action,
                details = details,
                metadata = metadata,
                severity = severity
            )
            
            transactionAuditTrails.getOrPut(transactionId) { mutableListOf() }.add(entry)
            
            auditLogger.logEvent(
                "AUDIT_TRAIL_ENTRY",
                "New audit trail entry for transaction: $transactionId",
                mapOf(
                    "action" to action,
                    "severity" to severity.name,
                    "timestamp" to entry.timestamp.toString()
                ) + metadata
            )
        }
    }

    /**
     * Analyzes a transaction and generates a forensics report
     */
    suspend fun analyzeTransaction(transactionId: String): ForensicsReport {
        forensicsMutex.withLock {
            val systemState = getCurrentSystemState()
            val error = recoveryService.getError(transactionId)
                ?: throw IllegalStateException("No error found for transaction: $transactionId")

            val report = ForensicsReport(
                transactionId = transactionId,
                timestamp = Instant.now(),
                error = error,
                isRecoverable = determineRecoverability(error, systemState),
                systemState = systemState,
                riskLevel = calculateRiskLevel(error, systemState),
                recommendedActions = generateRecommendedActions(error, systemState),
                details = mapOf(
                    "state_history" to getTransactionStateHistory(transactionId).toString(),
                    "blockchain_state" to systemState.blockchainState.toString(),
                    "network_status" to systemState.networkStatus.name,
                    "wallet_status" to systemState.walletStatus.name,
                    "escrow_status" to systemState.escrowStatus.name
                )
            )

            _forensicsReports.value = _forensicsReports.value + (transactionId to report)
            
            auditLogger.logEvent(
                "FORENSICS_REPORT_GENERATED",
                "Generated forensics report for transaction: $transactionId",
                mapOf(
                    "error_type" to (error::class.simpleName ?: "Unknown"),
                    "is_recoverable" to report.isRecoverable.toString(),
                    "risk_level" to report.riskLevel.name
                )
            )

            return report
        }
    }

    /**
     * Retrieves the complete audit trail for a transaction
     */
    suspend fun getAuditTrail(transactionId: String): List<AuditTrailEntry> {
        return forensicsMutex.withLock {
            transactionAuditTrails[transactionId]?.toList() ?: emptyList()
        }
    }

    /**
     * Gets the current system state
     */
    private suspend fun getCurrentSystemState(): SystemState {
        return SystemState(
            networkStatus = blockchainService.getNetworkStatus(),
            walletStatus = walletService.getWalletStatus(),
            escrowStatus = escrowService.getEscrowStatus(),
            blockchainState = blockchainService.getBlockchainState(),
            timestamp = Instant.now()
        )
    }

    /**
     * Determines if an error is recoverable based on its type and system state
     */
    private fun determineRecoverability(error: TransactionError, systemState: SystemState): Boolean {
        return when {
            systemState.networkStatus == NetworkStatus.OFFLINE -> false
            systemState.walletStatus == WalletStatus.ERROR -> false
            systemState.escrowStatus == EscrowStatus.ERROR -> false
            error.severity == ErrorSeverity.CRITICAL -> false
            else -> when (error) {
                is TransactionError.NetworkError -> systemState.networkStatus != NetworkStatus.OFFLINE
                is TransactionError.BlockchainError -> error.severity != ErrorSeverity.CRITICAL
                is TransactionError.WalletError -> systemState.walletStatus != WalletStatus.ERROR
                is TransactionError.ValidationError -> true
                is TransactionError.TimeoutError -> true
                is TransactionError.UnknownError -> false
            }
        }
    }

    /**
     * Calculates the risk level for a transaction error
     */
    private fun calculateRiskLevel(error: TransactionError, systemState: SystemState): RiskLevel {
        return when {
            error.severity == ErrorSeverity.CRITICAL -> RiskLevel.CRITICAL
            systemState.networkStatus == NetworkStatus.OFFLINE -> RiskLevel.CRITICAL
            systemState.walletStatus == WalletStatus.ERROR -> RiskLevel.CRITICAL
            systemState.escrowStatus == EscrowStatus.ERROR -> RiskLevel.CRITICAL
            error.severity == ErrorSeverity.HIGH -> RiskLevel.HIGH
            systemState.networkStatus == NetworkStatus.DEGRADED -> RiskLevel.HIGH
            error.severity == ErrorSeverity.MEDIUM -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    /**
     * Generates recommended actions based on error analysis
     */
    private fun generateRecommendedActions(error: TransactionError, systemState: SystemState): List<String> {
        val recommendations = mutableListOf<String>()

        when {
            systemState.networkStatus == NetworkStatus.OFFLINE -> {
                recommendations.add("Wait for network connectivity to be restored")
                recommendations.add("Check internet connection")
            }
            systemState.walletStatus == WalletStatus.ERROR -> {
                recommendations.add("Verify wallet connection")
                recommendations.add("Check wallet balance")
            }
            systemState.escrowStatus == EscrowStatus.ERROR -> {
                recommendations.add("Verify escrow contract status")
                recommendations.add("Check escrow conditions")
            }
            else -> when (error) {
                is TransactionError.NetworkError -> {
                    recommendations.add("Wait for network conditions to improve")
                    recommendations.add("Consider retrying with higher fees")
                }
                is TransactionError.BlockchainError -> {
                    recommendations.add("Review blockchain state")
                    recommendations.add("Check transaction parameters")
                }
                is TransactionError.WalletError -> {
                    recommendations.add("Verify wallet connection")
                    recommendations.add("Check wallet permissions")
                }
                is TransactionError.ValidationError -> {
                    recommendations.add("Review transaction parameters")
                    recommendations.add("Check validation requirements")
                }
                is TransactionError.TimeoutError -> {
                    recommendations.add("Retry transaction")
                    recommendations.add("Check network conditions")
                }
                is TransactionError.UnknownError -> {
                    recommendations.add("Contact system administrator")
                    recommendations.add("Review system logs")
                }
            }
        }

        return recommendations
    }

    private suspend fun getTransactionStateHistory(transactionId: String): List<String> {
        return recoveryService.getTransactionHistory(transactionId)
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
     * Updates error patterns for trend analysis and records the pattern in audit trail
     */
    private suspend fun updateErrorPatterns(error: TransactionError) {
        val patterns = errorPatterns.getOrPut(error::class.simpleName ?: "Unknown") { mutableListOf() }
        (patterns as MutableList).add(error)
        
        val frequency = patterns.size
        val firstOccurrence = patterns.minByOrNull { it.timestamp }?.timestamp
        val lastOccurrence = patterns.maxByOrNull { it.timestamp }?.timestamp
        
        recordAuditTrail(
            transactionId = error.transactionId,
            action = "ERROR_PATTERN_UPDATE",
            details = "Updated error pattern analysis",
            metadata = mapOf(
                "pattern_type" to (error::class.simpleName ?: "Unknown"),
                "frequency" to frequency.toString(),
                "first_occurrence" to (firstOccurrence?.toString() ?: "N/A"),
                "last_occurrence" to (lastOccurrence?.toString() ?: "N/A")
            )
        )
    }

    /**
     * Enhanced error pattern analysis with machine learning-based prediction
     */
    private suspend fun analyzeErrorPatterns(error: TransactionError): List<ErrorPattern> {
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
        
        // Update pattern cache
        errorPatternCache[error.transactionId] = patterns
        
        // Analyze time-based patterns
        val timeBasedPatterns = patterns
            .groupBy { error -> error.timestamp.atZone(ZoneId.systemDefault()).toLocalDate() }
            .mapValues { it.value.size }

        // Detect pattern trends
        val trends = detectPatternTrends(timeBasedPatterns)
        
        // Update realtime analytics
        updateRealtimeAnalytics(error, patterns)
        
        // Generate predictions
        generateErrorPredictions(error, patterns, trends)
        
        // Log significant patterns
        if (trends.isSignificantIncrease) {
            auditLogger.logEvent(
                "ERROR_PATTERN_DETECTED",
                "Detected significant increase in error frequency",
                mapOf(
                    "error_type" to error::class.simpleName,
                    "pattern_size" to patterns.size.toString(),
                    "trend_factor" to trends.increaseFactor.toString(),
                    "prediction_confidence" to trends.confidence.toString(),
                    "timestamp" to Instant.now().toString()
                )
            )
        }
        
        return patterns
    }

    /**
     * Detects complex pattern trends in error occurrences
     */
    private fun detectPatternTrends(timeBasedPatterns: Map<*, Int>): PatternTrends {
        if (timeBasedPatterns.size < 2) return PatternTrends()
        
        val values = timeBasedPatterns.values.toList()
        val recentAverage = values.takeLast(2).average()
        val previousAverage = values.dropLast(2).takeLastOrNull() ?: return PatternTrends()
        
        val increaseFactor = recentAverage / previousAverage
        val isSignificant = increaseFactor > 1.5
        
        // Calculate trend confidence based on sample size and consistency
        val confidence = calculateTrendConfidence(values)
        
        return PatternTrends(
            isSignificantIncrease = isSignificant,
            increaseFactor = increaseFactor,
            confidence = confidence
        )
    }

    /**
     * Updates realtime analytics based on new error data
     */
    private suspend fun updateRealtimeAnalytics(error: TransactionError, patterns: List<ErrorPattern>) {
        val currentAnalytics = realtimeAnalytics.value
        val activeTransactions = stellarTransactionManager.getActiveTransactions().size
        val errorRate = calculateErrorRate(activeTransactions, patterns.size)
        val avgResponseTime = calculateAverageResponseTime()
        val networkHealth = calculateNetworkHealth(errorRate, avgResponseTime)
        
        realtimeAnalytics.value = RealtimeAnalytics(
            activeTransactions = activeTransactions,
            errorRate = errorRate,
            avgResponseTime = avgResponseTime,
            networkHealth = networkHealth,
            timestamp = Instant.now()
        )
    }

    /**
     * Generates predictive error analysis using historical patterns
     */
    private suspend fun generateErrorPredictions(
        error: TransactionError,
        patterns: List<ErrorPattern>,
        trends: PatternTrends
    ) {
        val prediction = ErrorPrediction(
            transactionId = error.transactionId,
            predictedErrorType = predictNextErrorType(patterns),
            probability = calculateErrorProbability(patterns, trends),
            factors = determineRiskFactors(error, patterns),
            timestamp = Instant.now()
        )
        
        predictiveModel[error.transactionId] = prediction
        
        if (prediction.probability > HIGH_RISK_THRESHOLD) {
            auditLogger.logEvent(
                "HIGH_RISK_PREDICTION",
                "High probability of future errors detected",
                mapOf(
                    "transaction_id" to error.transactionId,
                    "predicted_error" to prediction.predictedErrorType,
                    "probability" to prediction.probability.toString(),
                    "risk_factors" to prediction.factors.joinToString(", ")
                )
            )
        }
    }

    private fun calculateTrendConfidence(values: List<Int>): Float {
        val sampleSize = values.size
        val variance = calculateVariance(values)
        return (1.0f - variance.coerceIn(0f, 1f)) * (sampleSize.toFloat() / 10f).coerceIn(0f, 1f)
    }

    private fun calculateVariance(values: List<Int>): Float {
        val mean = values.average()
        val squaredDiffs = values.map { (it - mean).pow(2) }
        return (squaredDiffs.sum() / values.size).toFloat()
    }

    private fun calculateErrorRate(activeTransactions: Int, errorCount: Int): Float {
        return if (activeTransactions > 0) {
            errorCount.toFloat() / activeTransactions
        } else 0f
    }

    private suspend fun calculateAverageResponseTime(): Long {
        val transactions = stellarTransactionManager.getRecentTransactions(10)
        return transactions.map { it.responseTime }.average().toLong()
    }

    private fun calculateNetworkHealth(errorRate: Float, avgResponseTime: Long): Float {
        val errorFactor = 1f - errorRate.coerceIn(0f, 1f)
        val responseFactor = 1f - (avgResponseTime.toFloat() / MAX_ACCEPTABLE_RESPONSE_TIME).coerceIn(0f, 1f)
        return (errorFactor + responseFactor) / 2f
    }

    private fun predictNextErrorType(patterns: List<ErrorPattern>): String {
        val errorTypes = patterns.groupBy { it.description }
        return errorTypes.maxByOrNull { it.value.size }?.key ?: "Unknown"
    }

    private fun calculateErrorProbability(patterns: List<ErrorPattern>, trends: PatternTrends): Float {
        val baseProb = patterns.size.toFloat() / MAX_PATTERN_THRESHOLD
        val trendFactor = if (trends.isSignificantIncrease) 1.5f else 1f
        return (baseProb * trendFactor * trends.confidence).coerceIn(0f, 1f)
    }

    private fun determineRiskFactors(error: TransactionError, patterns: List<ErrorPattern>): List<String> {
        val factors = mutableListOf<String>()
        
        if (patterns.size > ERROR_PATTERN_THRESHOLD) {
            factors.add("High error frequency")
        }
        
        when (error) {
            is NetworkCongestionError -> factors.add("Network congestion")
            is SmartContractError -> factors.add("Smart contract issues")
            is EscrowError -> factors.add("Escrow verification problems")
            is WalletConnectionError -> factors.add("Wallet connectivity issues")
        }
        
        return factors
    }

    data class PatternTrends(
        val isSignificantIncrease: Boolean = false,
        val increaseFactor: Double = 1.0,
        val confidence: Float = 0f
    )

    fun monitorSystemHealth(): Flow<SystemState> = flow {
        while (true) {
            val systemState = getCurrentSystemState()
            emit(systemState)
            kotlinx.coroutines.delay(SYSTEM_HEALTH_CHECK_INTERVAL)
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

    companion object {
        private const val ERROR_PATTERN_THRESHOLD = 5
        private const val SYSTEM_HEALTH_CHECK_INTERVAL = 30_000L // 30 seconds
        private const val HIGH_CONGESTION_THRESHOLD = 0.8f
        private const val MEDIUM_CONGESTION_THRESHOLD = 0.5f
        private const val MAX_PATTERN_THRESHOLD = 20
        private const val HIGH_RISK_THRESHOLD = 0.7f
        private const val MAX_ACCEPTABLE_RESPONSE_TIME = 5000L
    }
}

data class ForensicsReport(
    val transactionId: String,
    val timestamp: Instant,
    val error: TransactionError,
    val isRecoverable: Boolean,
    val systemState: SystemState,
    val riskLevel: RiskLevel,
    val recommendedActions: List<String>,
    val details: Map<String, Any>
)

data class SystemState(
    val networkStatus: NetworkStatus,
    val walletStatus: WalletStatus,
    val escrowStatus: EscrowStatus,
    val blockchainState: BlockchainState,
    val timestamp: Instant = Instant.now()
)

enum class NetworkStatus {
    HEALTHY,
    CONGESTED,
    DEGRADED,
    OFFLINE
}

enum class WalletStatus {
    CONNECTED,
    DISCONNECTED,
    SYNCING,
    ERROR
}

enum class EscrowStatus {
    ACTIVE,
    INACTIVE,
    LOCKED,
    ERROR
}

data class BlockchainState(
    val blockHeight: Long,
    val networkId: String,
    val gasPrice: Double,
    val congestionLevel: CongestionLevel,
    val timestamp: Instant = Instant.now()
)

enum class CongestionLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

interface TransactionForensicsService {
    /**
     * Analyzes a transaction and generates a forensics report
     */
    suspend fun analyzeTransaction(transactionId: String): ForensicsReport

    /**
     * Gets the current system state
     */
    suspend fun getCurrentSystemState(): SystemState

    /**
     * Updates the forensics analysis with new data
     */
    suspend fun updateForensics(
        transactionId: String,
        error: TransactionError,
        systemState: SystemState
    ): ForensicsReport

    /**
     * Gets the current forensics report for a transaction
     */
    fun getForensicsReport(transactionId: String): StateFlow<ForensicsReport?>

    /**
     * Calculates the risk level for a transaction error
     */
    fun calculateRiskLevel(error: TransactionError, systemState: SystemState): RiskLevel
}

/**
 * Represents an entry in the audit trail
 */
data class AuditTrailEntry(
    val timestamp: Instant,
    val action: String,
    val details: String,
    val metadata: Map<String, String>,
    val severity: AuditSeverity
)

/**
 * Represents the severity level of an audit trail entry
 */
enum class AuditSeverity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
} 