package com.example.fideicomisoapproverring.recovery.model

import java.time.Instant

/**
 * Base interface for all transaction errors in the Revolutionary Farmers marketplace.
 * This provides a common structure for error handling and analysis.
 */
interface TransactionError {
    val message: String
    val timestamp: Instant
    val severity: ErrorSeverity
    val recoverable: Boolean
}

/**
 * Represents a transaction error in the Revolutionary Farmers marketplace.
 * This sealed class hierarchy provides comprehensive error categorization and tracking.
 */
sealed class TransactionError(
    open val transactionId: String,
    open val message: String,
    val severity: ErrorSeverity = ErrorSeverity.HIGH,
    val recoverable: Boolean = true
) {
    data class NetworkCongestionError(
        override val transactionId: String,
        override val message: String,
        val retryAfter: Long,
        val networkLatency: Long,
        val congestionLevel: CongestionLevel
    ) : TransactionError(transactionId, message, ErrorSeverity.MEDIUM)

    data class InsufficientFundsError(
        override val transactionId: String,
        override val message: String,
        val requiredAmount: String,
        val availableAmount: String,
        val currency: String = "XLM"
    ) : TransactionError(transactionId, message)

    data class SmartContractError(
        override val transactionId: String,
        override val message: String,
        val contractAddress: String,
        val errorCode: String,
        val functionName: String,
        val parameters: Map<String, String> = emptyMap()
    ) : TransactionError(transactionId, message)

    data class WalletConnectionError(
        override val transactionId: String,
        override val message: String,
        val lastConnectedTimestamp: Long?,
        val connectionAttempts: Int,
        val walletType: String
    ) : TransactionError(transactionId, message, ErrorSeverity.MEDIUM)

    data class EscrowError(
        override val transactionId: String,
        override val message: String,
        val escrowContractAddress: String,
        val escrowState: String,
        val participantAddresses: List<String>
    ) : TransactionError(transactionId, message)

    data class BlockchainError(
        override val transactionId: String,
        override val message: String,
        val blockHeight: Long,
        val gasPrice: String?,
        val nonce: Long?
    ) : TransactionError(transactionId, message)

    data class TimeoutError(
        override val transactionId: String,
        override val message: String,
        val timeoutDuration: Long,
        val operationType: String
    ) : TransactionError(transactionId, message, ErrorSeverity.MEDIUM)

    data class UnknownError(
        override val transactionId: String,
        override val message: String,
        val stackTrace: String
    ) : TransactionError(transactionId, message, ErrorSeverity.CRITICAL, false)
}

/**
 * Severity levels for error classification
 */
enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Network congestion levels
 */
enum class CongestionLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Categorizes different types of transaction errors.
 */
enum class TransactionErrorType {
    BLOCKCHAIN_NETWORK_CONGESTION,
    INSUFFICIENT_FUNDS,
    SMART_CONTRACT_FAILURE,
    WALLET_CONNECTION_LOST,
    ESCROW_VERIFICATION_FAILED,
    API_COMMUNICATION_ERROR,
    SYSTEM_SYNCHRONIZATION_ERROR,
    UNKNOWN
}

/**
 * Represents the current status of a transaction error.
 */
enum class TransactionErrorStatus {
    ANALYZING,
    RECOVERING,
    RECOVERED,
    FAILED,
    MANUAL_INTERVENTION_REQUIRED,
    NOT_FOUND
}

/**
 * Defines different strategies for recovering from transaction errors.
 */
enum class TransactionRecoveryStrategy {
    RETRY,                    // Simple retry of the transaction
    ROLLBACK,                // Complete rollback of the transaction
    PARTIAL_ROLLBACK,        // Rollback specific parts of the transaction
    COMPENSATING_ACTION,     // Perform a compensating action
    MANUAL_RESOLUTION,       // Requires manual intervention
    WAIT_AND_RETRY,          // Wait for a specific condition and retry
    ESCALATE                 // Escalate to system administrators
} 