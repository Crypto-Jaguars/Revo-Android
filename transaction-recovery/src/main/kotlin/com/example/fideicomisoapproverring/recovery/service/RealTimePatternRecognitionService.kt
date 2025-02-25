package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.forensics.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class RealTimePatternRecognitionService @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val auditLogger: SecureAuditLogger,
    private val forensicsService: TransactionForensicsService
) {
    private val _patternState = MutableStateFlow<PatternRecognitionState>(PatternRecognitionState())
    val patternState: StateFlow<PatternRecognitionState> = _patternState.asStateFlow()

    data class PatternRecognitionState(
        val activePatterns: List<ErrorPattern> = emptyList(),
        val riskLevel: RiskLevel = RiskLevel.LOW,
        val detectedAnomalies: List<Anomaly> = emptyList(),
        val lastUpdate: Instant = Instant.now()
    )

    data class ErrorPattern(
        val patternId: String,
        val errorType: String,
        val frequency: Int,
        val timeWindow: Long, // in minutes
        val affectedTransactions: Set<String>,
        val firstOccurrence: Instant,
        val lastOccurrence: Instant,
        val severity: ErrorSeverity,
        val correlatedFactors: Map<String, Float> // factor -> correlation coefficient
    )

    data class Anomaly(
        val id: String,
        val type: AnomalyType,
        val description: String,
        val detectedAt: Instant,
        val confidence: Float,
        val relatedPatterns: List<String>
    )

    enum class AnomalyType {
        FREQUENCY_SPIKE,
        PATTERN_SHIFT,
        SEVERITY_ESCALATION,
        CORRELATION_CHANGE,
        TIMING_ANOMALY
    }

    enum class RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    suspend fun processError(transactionId: String, error: TransactionError) {
        val currentState = _patternState.value
        val newPattern = createOrUpdatePattern(error, currentState.activePatterns)
        val anomalies = detectAnomalies(newPattern, currentState.activePatterns)
        val riskLevel = calculateRiskLevel(newPattern, anomalies)

        val updatedPatterns = updatePatterns(currentState.activePatterns, newPattern)
        
        _patternState.value = currentState.copy(
            activePatterns = updatedPatterns,
            riskLevel = riskLevel,
            detectedAnomalies = anomalies,
            lastUpdate = Instant.now()
        )

        logPatternUpdate(newPattern, anomalies, riskLevel)
    }

    private suspend fun createOrUpdatePattern(
        error: TransactionError,
        existingPatterns: List<ErrorPattern>
    ): ErrorPattern {
        val existingPattern = existingPatterns.find { 
            it.errorType == error::class.simpleName && 
            ChronoUnit.MINUTES.between(it.lastOccurrence, error.timestamp) <= PATTERN_TIME_WINDOW 
        }

        return if (existingPattern != null) {
            existingPattern.copy(
                frequency = existingPattern.frequency + 1,
                lastOccurrence = error.timestamp,
                affectedTransactions = existingPattern.affectedTransactions + error.transactionId,
                correlatedFactors = updateCorrelationFactors(existingPattern, error)
            )
        } else {
            ErrorPattern(
                patternId = generatePatternId(error),
                errorType = error::class.simpleName ?: "Unknown",
                frequency = 1,
                timeWindow = PATTERN_TIME_WINDOW,
                affectedTransactions = setOf(error.transactionId),
                firstOccurrence = error.timestamp,
                lastOccurrence = error.timestamp,
                severity = error.severity,
                correlatedFactors = calculateInitialCorrelations(error)
            )
        }
    }

    private suspend fun detectAnomalies(
        newPattern: ErrorPattern,
        existingPatterns: List<ErrorPattern>
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        // Frequency spike detection
        if (isFrequencyAnomaly(newPattern, existingPatterns)) {
            anomalies.add(createFrequencyAnomaly(newPattern))
        }

        // Pattern shift detection
        if (isPatternShift(newPattern, existingPatterns)) {
            anomalies.add(createPatternShiftAnomaly(newPattern))
        }

        // Severity escalation detection
        if (isSeverityEscalation(newPattern, existingPatterns)) {
            anomalies.add(createSeverityAnomaly(newPattern))
        }

        // Correlation change detection
        val correlationChanges = detectCorrelationChanges(newPattern, existingPatterns)
        if (correlationChanges.isNotEmpty()) {
            anomalies.addAll(correlationChanges)
        }

        return anomalies
    }

    private fun calculateRiskLevel(pattern: ErrorPattern, anomalies: List<Anomaly>): RiskLevel {
        val severityScore = when (pattern.severity) {
            ErrorSeverity.CRITICAL -> 4
            ErrorSeverity.HIGH -> 3
            ErrorSeverity.MEDIUM -> 2
            ErrorSeverity.LOW -> 1
        }

        val anomalyScore = anomalies.size * 0.5f
        val frequencyScore = (pattern.frequency / FREQUENCY_THRESHOLD.toFloat()).coerceAtMost(1f) * 2

        val totalScore = severityScore + anomalyScore + frequencyScore

        return when {
            totalScore >= 6 -> RiskLevel.CRITICAL
            totalScore >= 4 -> RiskLevel.HIGH
            totalScore >= 2 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    private suspend fun logPatternUpdate(
        pattern: ErrorPattern,
        anomalies: List<Anomaly>,
        riskLevel: RiskLevel
    ) {
        auditLogger.logEvent(
            "PATTERN_RECOGNITION_UPDATE",
            "Updated error pattern analysis",
            mapOf(
                "pattern_id" to pattern.patternId,
                "error_type" to pattern.errorType,
                "frequency" to pattern.frequency.toString(),
                "risk_level" to riskLevel.name,
                "anomalies_detected" to anomalies.size.toString(),
                "timestamp" to Instant.now().toString()
            )
        )

        if (anomalies.isNotEmpty()) {
            auditLogger.logEvent(
                "ANOMALIES_DETECTED",
                "Detected anomalies in error patterns",
                mapOf(
                    "anomaly_count" to anomalies.size.toString(),
                    "anomaly_types" to anomalies.map { it.type.name }.joinToString(","),
                    "confidence_levels" to anomalies.map { it.confidence }.joinToString(",")
                )
            )
        }
    }

    private fun generatePatternId(error: TransactionError): String =
        "${error::class.simpleName}_${error.severity}_${Instant.now().toEpochMilli()}"

    private suspend fun updateCorrelationFactors(
        pattern: ErrorPattern,
        error: TransactionError
    ): Map<String, Float> {
        val currentFactors = pattern.correlatedFactors.toMutableMap()
        val systemState = forensicsService.getCurrentSystemState()

        // Update network correlation
        currentFactors["network_congestion"] = calculateCorrelation(
            systemState.networkStatus,
            pattern.frequency
        )

        // Update blockchain correlation
        currentFactors["blockchain_state"] = calculateCorrelation(
            systemState.blockchainState.networkCongestion,
            pattern.frequency
        )

        return currentFactors
    }

    private fun calculateCorrelation(factor: Any, frequency: Int): Float {
        // Simplified correlation calculation
        return when (factor) {
            is NetworkStatus -> when (factor) {
                NetworkStatus.HEALTHY -> 0.1f
                NetworkStatus.DEGRADED -> 0.5f
                NetworkStatus.CONGESTED -> 0.8f
                NetworkStatus.OFFLINE -> 1.0f
                else -> 0.5f
            }
            is Float -> factor.coerceIn(0f, 1f)
            else -> 0.5f
        }
    }

    private fun calculateInitialCorrelations(error: TransactionError): Map<String, Float> {
        return mapOf(
            "network_congestion" to 0.5f,
            "blockchain_state" to 0.5f,
            "time_of_day" to 0.5f
        )
    }

    private fun updatePatterns(
        existingPatterns: List<ErrorPattern>,
        newPattern: ErrorPattern
    ): List<ErrorPattern> {
        val updatedPatterns = existingPatterns.toMutableList()
        val existingIndex = updatedPatterns.indexOfFirst { it.patternId == newPattern.patternId }
        
        if (existingIndex >= 0) {
            updatedPatterns[existingIndex] = newPattern
        } else {
            updatedPatterns.add(newPattern)
        }
        
        // Remove patterns older than the time window
        val cutoffTime = Instant.now().minus(PATTERN_TIME_WINDOW, ChronoUnit.MINUTES)
        return updatedPatterns.filter { it.lastOccurrence.isAfter(cutoffTime) }
    }

    private fun isFrequencyAnomaly(
        newPattern: ErrorPattern,
        existingPatterns: List<ErrorPattern>
    ): Boolean {
        val similarPatterns = existingPatterns.filter { 
            it.errorType == newPattern.errorType &&
            it.patternId != newPattern.patternId 
        }
        
        if (similarPatterns.isEmpty()) return false
        
        val avgFrequency = similarPatterns.map { it.frequency }.average()
        return newPattern.frequency > avgFrequency * 2
    }

    private fun createFrequencyAnomaly(pattern: ErrorPattern): Anomaly {
        return Anomaly(
            id = "freq_${pattern.patternId}_${Instant.now().toEpochMilli()}",
            type = AnomalyType.FREQUENCY_SPIKE,
            description = "Unusual increase in error frequency detected",
            detectedAt = Instant.now(),
            confidence = 0.8f,
            relatedPatterns = listOf(pattern.patternId)
        )
    }

    private fun isPatternShift(
        newPattern: ErrorPattern,
        existingPatterns: List<ErrorPattern>
    ): Boolean {
        val previousPatterns = existingPatterns.filter { 
            it.errorType == newPattern.errorType &&
            it.patternId != newPattern.patternId 
        }
        
        if (previousPatterns.isEmpty()) return false
        
        val avgCorrelations = previousPatterns
            .flatMap { it.correlatedFactors.entries }
            .groupBy { it.key }
            .mapValues { it.value.map { entry -> entry.value }.average() }
        
        return newPattern.correlatedFactors.any { (factor, correlation) ->
            val avgCorrelation = avgCorrelations[factor] ?: return@any false
            abs(correlation - avgCorrelation) > 0.3
        }
    }

    private fun createPatternShiftAnomaly(pattern: ErrorPattern): Anomaly {
        return Anomaly(
            id = "shift_${pattern.patternId}_${Instant.now().toEpochMilli()}",
            type = AnomalyType.PATTERN_SHIFT,
            description = "Significant shift in error pattern correlations detected",
            detectedAt = Instant.now(),
            confidence = 0.75f,
            relatedPatterns = listOf(pattern.patternId)
        )
    }

    private fun isSeverityEscalation(
        newPattern: ErrorPattern,
        existingPatterns: List<ErrorPattern>
    ): Boolean {
        val previousPatterns = existingPatterns.filter { 
            it.errorType == newPattern.errorType &&
            it.patternId != newPattern.patternId 
        }
        
        if (previousPatterns.isEmpty()) return false
        
        val maxPreviousSeverity = previousPatterns.maxOf { it.severity.ordinal }
        return newPattern.severity.ordinal > maxPreviousSeverity
    }

    private fun createSeverityAnomaly(pattern: ErrorPattern): Anomaly {
        return Anomaly(
            id = "sev_${pattern.patternId}_${Instant.now().toEpochMilli()}",
            type = AnomalyType.SEVERITY_ESCALATION,
            description = "Error severity has escalated beyond historical levels",
            detectedAt = Instant.now(),
            confidence = 0.9f,
            relatedPatterns = listOf(pattern.patternId)
        )
    }

    private fun detectCorrelationChanges(
        newPattern: ErrorPattern,
        existingPatterns: List<ErrorPattern>
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()
        val previousPatterns = existingPatterns.filter { 
            it.errorType == newPattern.errorType &&
            it.patternId != newPattern.patternId 
        }
        
        if (previousPatterns.isEmpty()) return anomalies
        
        val significantChanges = newPattern.correlatedFactors.filter { (factor, correlation) ->
            val previousCorrelations = previousPatterns.mapNotNull { 
                it.correlatedFactors[factor] 
            }
            
            if (previousCorrelations.isEmpty()) return@filter false
            
            val avgCorrelation = previousCorrelations.average()
            abs(correlation - avgCorrelation) > 0.3
        }
        
        significantChanges.forEach { (factor, correlation) ->
            anomalies.add(
                Anomaly(
                    id = "corr_${newPattern.patternId}_${factor}_${Instant.now().toEpochMilli()}",
                    type = AnomalyType.CORRELATION_CHANGE,
                    description = "Significant change in correlation with factor: $factor",
                    detectedAt = Instant.now(),
                    confidence = 0.85f,
                    relatedPatterns = listOf(newPattern.patternId)
                )
            )
        }
        
        return anomalies
    }

    companion object {
        private const val PATTERN_TIME_WINDOW = 60L // 60 minutes
        private const val FREQUENCY_THRESHOLD = 10
        private const val ANOMALY_CONFIDENCE_THRESHOLD = 0.7f
    }
} 