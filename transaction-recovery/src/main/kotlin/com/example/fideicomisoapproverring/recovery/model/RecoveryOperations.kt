package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents different types of recovery operations that can be performed
 */
enum class RecoveryOperation {
    VIEW_STATUS,
    INITIATE_RECOVERY,
    CANCEL_RECOVERY,
    ROLLBACK_TRANSACTION,
    MANUAL_INTERVENTION
}

/**
 * Represents fine-grained permissions for recovery operations
 */
enum class RecoveryPermission {
    READ_BASIC,           // Basic read access to recovery status
    READ_DETAILS,         // Detailed read access including sensitive information
    INITIATE,            // Permission to start recovery process
    CANCEL,              // Permission to cancel recovery process
    ROLLBACK,            // Permission to rollback transactions
    MANUAL_INTERVENTION, // Permission to perform manual intervention
    SECURITY_OVERRIDE    // Override security restrictions (Security Admin only)
}

/**
 * Represents the result of a recovery operation access check
 */
data class RecoveryAccessResult(
    val isAllowed: Boolean,
    val denialReason: String? = null,
    val requiredPermissions: Set<RecoveryPermission>? = null
) 