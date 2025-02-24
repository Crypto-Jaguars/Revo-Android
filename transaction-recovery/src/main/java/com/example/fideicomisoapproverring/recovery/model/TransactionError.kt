package com.example.fideicomisoapproverring.recovery.model

import java.util.UUID

/**
 * Represents a transaction error in the Revolutionary Farmers marketplace.
 */
sealed class TransactionError(
    val id: String,
    val transactionId: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NetworkCongestionError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

class InsufficientFundsError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

class SmartContractError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

class WalletConnectionError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

class EscrowVerificationError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

class SystemSynchronizationError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

class UnknownError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

class RecoveredError(
    id: String,
    transactionId: String,
    message: String
) : TransactionError(id, transactionId, message)

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