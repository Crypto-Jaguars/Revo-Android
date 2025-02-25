package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents the current status of a transaction recovery process.
 */
sealed class RecoveryStatus {
    object Analyzing : RecoveryStatus()
    
    data class InProgress(
        val progress: Int,
        val stage: String,
        val details: String? = null
    ) : RecoveryStatus()
    
    data class RequiresAction(
        val actionType: ActionType,
        val message: String,
        val details: Map<String, String> = emptyMap()
    ) : RecoveryStatus()
    
    data class Completed(
        val result: TransactionStatus,
        val timestamp: Long = System.currentTimeMillis()
    ) : RecoveryStatus()
    
    data class Failed(
        val error: TransactionError,
        val attempts: Int,
        val canRetry: Boolean
    ) : RecoveryStatus()
}

enum class ActionType {
    WALLET_VERIFICATION,
    ESCROW_VERIFICATION,
    MANUAL_INTERVENTION,
    RETRY
}

enum class TransactionStatus {
    SUCCESSFUL,
    FAILED,
    ROLLED_BACK,
    PARTIALLY_COMPLETED
} 