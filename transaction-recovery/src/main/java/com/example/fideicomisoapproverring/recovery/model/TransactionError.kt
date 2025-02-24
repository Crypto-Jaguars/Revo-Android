package com.example.fideicomisoapproverring.recovery.model

import java.time.Instant

/**
 * Base interface for all transaction errors in the Revolutionary Farmers marketplace.
 * This provides a common structure for error handling and analysis.
 */
sealed interface TransactionError {
    val transactionId: String
    val message: String
    val timestamp: Instant
    val severity: ErrorSeverity
    val isRecoverable: Boolean

    data class NetworkCongestionError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        val retryAfter: Long,
        override val severity: ErrorSeverity = ErrorSeverity.MEDIUM,
        override val isRecoverable: Boolean = true
    ) : TransactionError

    data class SmartContractError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        val contractAddress: String,
        val errorCode: String,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH,
        override val isRecoverable: Boolean = false
    ) : TransactionError

    data class InsufficientFundsError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        val requiredAmount: String,
        val availableAmount: String,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH,
        override val isRecoverable: Boolean = true
    ) : TransactionError

    data class WalletConnectionError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        val walletId: String,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH,
        override val isRecoverable: Boolean = true
    ) : TransactionError

    data class EscrowError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        val escrowId: String,
        val escrowState: String,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH,
        override val isRecoverable: Boolean = true
    ) : TransactionError

    data class BlockchainError(
        override val transactionId: String,
        override val message: String,
        val blockHeight: Long,
        val gasPrice: String?,
        val nonce: Long?
    ) : TransactionError

    data class TimeoutError(
        override val transactionId: String,
        override val message: String,
        val timeoutDuration: Long,
        val operationType: String
    ) : TransactionError

    data class MaxRetriesExceeded(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        val attempts: Int,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH,
        override val isRecoverable: Boolean = false
    ) : TransactionError

    data class UnknownError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH,
        override val isRecoverable: Boolean = false
    ) : TransactionError
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