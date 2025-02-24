package com.example.fideicomisoapproverring.recovery.model

import java.time.Instant

/**
 * Represents the severity level of a transaction error
 */
enum class ErrorSeverity {
    LOW,      // Minor issues that don't affect transaction integrity
    MEDIUM,   // Issues that may affect transaction performance
    HIGH,     // Serious issues requiring immediate attention
    CRITICAL  // Critical failures that could lead to financial loss
}

/**
 * Represents a network congestion error
 */
data class NetworkCongestionError(
    override val message: String,
    override val timestamp: Instant = Instant.now(),
    val congestionLevel: CongestionLevel,
    override val severity: ErrorSeverity = ErrorSeverity.MEDIUM,
    override val recoverable: Boolean = true
) : TransactionError

/**
 * Represents a smart contract execution error
 */
data class SmartContractError(
    override val message: String,
    override val timestamp: Instant = Instant.now(),
    val contractAddress: String,
    val functionName: String,
    override val severity: ErrorSeverity = ErrorSeverity.HIGH,
    override val recoverable: Boolean = false
) : TransactionError

/**
 * Represents an escrow-related error
 */
data class EscrowError(
    override val message: String,
    override val timestamp: Instant = Instant.now(),
    val escrowAccount: String,
    val missingSignatures: List<String>,
    override val severity: ErrorSeverity = ErrorSeverity.HIGH,
    override val recoverable: Boolean = true
) : TransactionError

/**
 * Represents the network congestion level
 */
enum class CongestionLevel {
    LOW,     // Network operating normally
    MEDIUM,  // Some delays expected
    HIGH,    // Significant delays
    EXTREME  // Network nearly unusable
}

/**
 * Utility function to detect significant increases in error frequency
 */
fun detectSignificantIncrease(timeBasedPatterns: Map<*, Int>): Boolean {
    if (timeBasedPatterns.size < 2) return false
    
    val values = timeBasedPatterns.values.toList()
    val recentAverage = values.takeLast(2).average()
    val previousAverage = values.dropLast(2).takeLastOrNull() ?: return false
    
    return recentAverage > previousAverage * 1.5
} 