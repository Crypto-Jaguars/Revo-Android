package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents the result of a transaction recovery attempt.
 */
data class RecoveryResult(
    val status: TransactionErrorStatus,
    val message: String,
    val error: TransactionError?,
    val recoveryDetails: Map<String, Any> = emptyMap()
) 