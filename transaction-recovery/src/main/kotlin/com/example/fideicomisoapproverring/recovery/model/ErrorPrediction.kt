package com.example.fideicomisoapproverring.recovery.model

import java.time.Instant

/**
 * Represents a machine learning-based error prediction for a transaction
 */
data class ErrorPrediction(
    val predictedErrorType: String,
    val probability: Float,
    val riskFactors: List<String>,
    val timestamp: Instant = Instant.now()
)

/**
 * Represents the confidence level of a prediction
 */
enum class PredictionConfidence {
    LOW,      // < 30% confidence
    MEDIUM,   // 30-70% confidence
    HIGH,     // 70-90% confidence
    VERY_HIGH // > 90% confidence
}

/**
 * Represents a risk factor category
 */
enum class RiskFactorCategory {
    NETWORK_CONGESTION,
    SMART_CONTRACT_COMPLEXITY,
    ESCROW_OPERATIONS,
    TRANSACTION_COMPLEXITY,
    WALLET_STATE,
    TIME_SENSITIVITY
} 