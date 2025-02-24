package com.example.fideicomisoapproverring.recovery.model

sealed class RollbackState {
    data class Analyzing(val transactionId: String) : RollbackState()
    data class InProgress(val transactionId: String, val progress: Int) : RollbackState()
    data class RequiresManualIntervention(
        val transactionId: String,
        val reason: String,
        val adminContact: String = "support@revolutionaryfarmers.com"
    ) : RollbackState()
    data class Completed(val transactionId: String, val refundTxId: String?) : RollbackState()
    data class Failed(
        val transactionId: String,
        val reason: String,
        val retryable: Boolean = false
    ) : RollbackState()
} 