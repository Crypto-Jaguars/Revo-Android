package com.example.fideicomisoapproverring.recovery.model

import com.example.fideicomisoapproverring.core.model.TransactionError
import java.time.Instant

/**
 * Represents the state of a recovery operation
 */
sealed class RecoveryState {
    data class Initial(val transactionId: String) : RecoveryState()
    data class Analyzing(val transactionId: String) : RecoveryState()
    data class RecoveryInProgress(
        val transactionId: String,
        val strategy: RecoveryStrategy,
        val attempt: Int,
        val maxAttempts: Int
    ) : RecoveryState()
    data class RecoveryComplete(val transactionId: String) : RecoveryState()
    data class Failed(
        val transactionId: String,
        val error: TransactionError,
        val reason: String
    ) : RecoveryState()
    data class RequiresAction(
        val transactionId: String,
        val actionType: ActionType,
        val message: String,
        val details: Map<String, String> = emptyMap()
    ) : RecoveryState()
}

/**
 * Types of recovery actions that may be required
 */
enum class ActionType {
    MANUAL_INTERVENTION,
    WALLET_CONNECTION,
    ESCROW_VERIFICATION,
    NETWORK_RETRY,
    SYSTEM_UPGRADE
}

/**
 * Represents a recovery strategy for handling transaction errors
 */
enum class RecoveryStrategy {
    RETRY,
    ROLLBACK,
    PARTIAL_ROLLBACK,
    COMPENSATING_ACTION,
    WAIT_AND_RETRY,
    MANUAL_RESOLUTION,
    ESCALATE
}

/**
 * Represents the status of a recovery operation
 */
enum class RecoveryStatus {
    ANALYZING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    REQUIRES_ACTION
}

/**
 * Represents a user action required during recovery
 */
data class UserAction(
    val actionType: ActionType,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val timestamp: Instant = Instant.now()
)

/**
 * Represents the permissions required for recovery operations
 */
enum class RecoveryPermission {
    READ_BASIC,
    READ_DETAILS,
    INITIATE,
    CANCEL,
    ROLLBACK,
    MANUAL_INTERVENTION,
    ADMIN
}

/**
 * Types of recovery operations
 */
enum class RecoveryOperation {
    VIEW_STATUS,
    INITIATE_RECOVERY,
    CANCEL_RECOVERY,
    ROLLBACK_TRANSACTION,
    MANUAL_INTERVENTION,
    ADMIN_OVERRIDE
}

/**
 * Represents an admin role in the recovery system
 */
enum class AdminRole {
    BASIC_OPERATOR,
    RECOVERY_SPECIALIST,
    SYSTEM_ADMIN,
    SECURITY_ADMIN
}

enum class NetworkStatus {
    HEALTHY,
    DEGRADED,
    CONGESTED,
    OFFLINE
}

enum class WalletStatus {
    CONNECTED,
    DISCONNECTED,
    ERROR
}

enum class EscrowStatus {
    ACTIVE,
    INACTIVE,
    ERROR
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class BlockchainState(
    val blockHeight: Long,
    val lastBlockTimestamp: Instant,
    val networkCongestion: Float,
    val averageTransactionTime: Long,
    val activeValidators: Int
)

data class SystemState(
    val networkStatus: NetworkStatus,
    val walletStatus: WalletStatus,
    val escrowStatus: EscrowStatus,
    val blockchainState: BlockchainState,
    val timestamp: Instant
)

data class ForensicsReport(
    val transactionId: String,
    val timestamp: Instant,
    val error: TransactionError,
    val isRecoverable: Boolean,
    val systemState: SystemState,
    val riskLevel: RiskLevel,
    val recommendedActions: List<String>,
    val details: Map<String, String>
) 