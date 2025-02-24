package com.example.fideicomisoapproverring.stellar.model

/**
 * Represents the current state of the Stellar network
 */
data class NetworkState(
    val congestion: Float, // Network congestion level (0.0 to 1.0)
    val averageTransactionTime: Long, // Average transaction confirmation time in milliseconds
    val currentBaseFee: Long, // Current base fee in stroops
    val ledgerVersion: Int, // Current ledger version
    val lastLedgerCloseTime: Long, // Timestamp of last ledger close
    val protocolVersion: Int, // Current protocol version
    val networkQuality: NetworkQuality = NetworkQuality.NORMAL
)

/**
 * Indicates the overall quality of network operations
 */
enum class NetworkQuality {
    EXCELLENT, // Fast confirmations, low fees
    NORMAL,    // Standard operation
    DEGRADED,  // Slower than normal, higher fees
    POOR       // Significant delays, very high fees
} 