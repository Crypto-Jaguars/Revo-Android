package com.example.fideicomisoapproverring.recovery.ml

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.pow

@Singleton
class TransactionErrorPredictor @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val auditLogger: SecureAuditLogger
) : TransactionErrorPredictor {
    private val _predictionState = MutableStateFlow<Map<String, ErrorPrediction>>(emptyMap())
    val predictionState: StateFlow<Map<String, ErrorPrediction>> = _predictionState.asStateFlow()

    // Historical data for training
    private val errorHistory = mutableListOf<ErrorDataPoint>()
    private val featureWeights = mutableMapOf<String, Float>()
    private var isModelTrained = false

    /**
     * Predicts potential errors for a transaction based on historical patterns
     */
    suspend fun predictErrors(transactionId: String): ErrorPrediction {
        try {
            val transaction = stellarTransactionManager.getTransaction(transactionId)
            val features = extractFeatures(transaction)
            
            if (!isModelTrained && errorHistory.size >= MIN_TRAINING_SAMPLES) {
                trainModel()
            }

            val prediction = if (isModelTrained) {
                predictErrorProbabilities(features)
            } else {
                // Fallback to basic prediction if model isn't trained
                basicPrediction(features)
            }

            _predictionState.value = _predictionState.value + (transactionId to prediction)
            logPrediction(transactionId, prediction)

            return prediction
        } catch (e: Exception) {
            handlePredictionError(transactionId, e)
            throw e
        }
    }

    /**
     * Records a new error occurrence for model training
     */
    suspend fun recordError(error: TransactionError) {
        val transaction = stellarTransactionManager.getTransaction(error.transactionId)
        val features = extractFeatures(transaction)
        
        val dataPoint = ErrorDataPoint(
            features = features,
            errorType = error::class.simpleName ?: "Unknown",
            timestamp = error.timestamp
        )
        
        errorHistory.add(dataPoint)
        pruneOldData()

        if (errorHistory.size >= MIN_TRAINING_SAMPLES) {
            trainModel()
        }
    }

    private fun extractFeatures(transaction: StellarTransaction): Map<String, Float> {
        return mapOf(
            "transaction_amount" to transaction.amount.toFloatOrNull() ?: 0f,
            "num_operations" to transaction.operations.size.toFloat(),
            "num_signatures" to transaction.signatures.size.toFloat(),
            "has_escrow" to if (transaction.operations.any { it.type == "manage_escrow" }) 1f else 0f,
            "has_smart_contract" to if (transaction.operations.any { it.type == "invoke_contract" }) 1f else 0f,
            "network_congestion" to calculateNetworkCongestion(),
            "time_of_day" to (Instant.now().toEpochMilli() % 86400000) / 86400000f
        )
    }

    private suspend fun calculateNetworkCongestion(): Float {
        return try {
            val recentTransactions = stellarTransactionManager.getRecentTransactions(100)
            val failedTransactions = recentTransactions.count { !it.isSuccessful() }
            (failedTransactions.toFloat() / recentTransactions.size).coerceIn(0f, 1f)
        } catch (e: Exception) {
            auditLogger.logEvent("CONGESTION_CALCULATION_ERROR", "Failed to calculate network congestion")
            0.5f // Default to medium congestion on error
        }
    }

    private fun trainModel() {
        if (errorHistory.size < MIN_TRAINING_SAMPLES) return

        // Calculate feature importance using a simple correlation-based approach
        val featureSet = errorHistory.first().features.keys
        featureSet.forEach { feature ->
            val correlation = calculateFeatureCorrelation(feature)
            featureWeights[feature] = correlation.coerceIn(0f, 1f)
        }

        isModelTrained = true
        auditLogger.logEvent(
            "MODEL_TRAINED",
            "Error prediction model trained",
            mapOf("num_samples" to errorHistory.size.toString())
        )
    }

    private fun calculateFeatureCorrelation(feature: String): Float {
        val featureValues = errorHistory.map { it.features[feature] ?: 0f }
        val errorOccurrences = errorHistory.groupBy { it.errorType }.mapValues { it.value.size.toFloat() }
        
        var correlation = 0f
        errorOccurrences.forEach { (_, count) ->
            val normalizedCount = count / errorHistory.size
            correlation += normalizedCount * calculateFeatureVariance(featureValues)
        }
        
        return correlation
    }

    private fun calculateFeatureVariance(values: List<Float>): Float {
        val mean = values.average()
        return values.map { (it - mean).pow(2) }.average().toFloat()
    }

    private fun predictErrorProbabilities(features: Map<String, Float>): ErrorPrediction {
        val errorTypeScores = mutableMapOf<String, Float>()
        val uniqueErrorTypes = errorHistory.map { it.errorType }.distinct()

        uniqueErrorTypes.forEach { errorType ->
            var score = 0f
            features.forEach { (feature, value) ->
                val weight = featureWeights[feature] ?: 0f
                score += value * weight
            }
            errorTypeScores[errorType] = sigmoid(score)
        }

        val mostLikelyError = errorTypeScores.maxByOrNull { it.value }
        val riskFactors = determineRiskFactors(features)

        return ErrorPrediction(
            predictedErrorType = mostLikelyError?.key ?: "Unknown",
            probability = mostLikelyError?.value ?: 0f,
            riskFactors = riskFactors,
            timestamp = Instant.now()
        )
    }

    private fun basicPrediction(features: Map<String, Float>): ErrorPrediction {
        // Simple heuristic-based prediction when not enough data
        val riskScore = features.values.average().toFloat()
        return ErrorPrediction(
            predictedErrorType = "Unknown",
            probability = riskScore.coerceIn(0f, 1f),
            riskFactors = determineRiskFactors(features),
            timestamp = Instant.now()
        )
    }

    private fun determineRiskFactors(features: Map<String, Float>): List<String> {
        return buildList {
            if ((features["network_congestion"] ?: 0f) > 0.7f) {
                add("High network congestion")
            }
            if ((features["has_smart_contract"] ?: 0f) > 0f) {
                add("Smart contract complexity")
            }
            if ((features["has_escrow"] ?: 0f) > 0f) {
                add("Escrow operations")
            }
            if ((features["num_operations"] ?: 0f) > 5f) {
                add("Multiple operations")
            }
        }
    }

    private fun sigmoid(x: Float): Float = 1f / (1f + exp(-x))

    private fun pruneOldData() {
        val cutoffTime = Instant.now().minus(DATA_RETENTION_DAYS, ChronoUnit.DAYS)
        errorHistory.removeAll { it.timestamp.isBefore(cutoffTime) }
    }

    private fun logPrediction(transactionId: String, prediction: ErrorPrediction) {
        auditLogger.logEvent(
            "ERROR_PREDICTION",
            "Generated error prediction for transaction: $transactionId",
            mapOf(
                "predicted_error" to prediction.predictedErrorType,
                "probability" to prediction.probability.toString(),
                "risk_factors" to prediction.riskFactors.joinToString(", ")
            )
        )
    }

    private fun handlePredictionError(transactionId: String, error: Exception) {
        auditLogger.logEvent(
            "PREDICTION_ERROR",
            "Error during error prediction for transaction: $transactionId",
            mapOf(
                "error_type" to error.javaClass.simpleName,
                "error_message" to error.message.toString()
            )
        )
    }

    data class ErrorDataPoint(
        val features: Map<String, Float>,
        val errorType: String,
        val timestamp: Instant
    )

    companion object {
        private const val MIN_TRAINING_SAMPLES = 50
        private const val DATA_RETENTION_DAYS = 30L
    }

    /**
     * Updates the prediction model with new error data
     */
    suspend fun updateModel(error: TransactionError) {
        recordError(error)
    }

    /**
     * Gets the current prediction accuracy
     */
    fun getPredictionAccuracy(): Float {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    /**
     * Gets the feature importance scores
     */
    fun getFeatureImportance(): Map<String, Float> {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }
} 