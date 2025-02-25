package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents the state of a transaction rollback operation.
 * This sealed class hierarchy provides comprehensive tracking of rollback progress.
 */
sealed class RollbackState {
    abstract val transactionId: String

    data class Analyzing(
        override val transactionId: String
    ) : RollbackState()

    data class InProgress(
        override val transactionId: String,
        val currentStep: String,
        val progress: Int
    ) : RollbackState()

    data class RequiresManualIntervention(
        override val transactionId: String,
        val reason: String,
        val suggestedAction: String
    ) : RollbackState()

    data class Completed(
        override val transactionId: String,
        val rollbackTransactionId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : RollbackState()

    data class Failed(
        override val transactionId: String,
        val reason: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : RollbackState()

    data class PartiallyCompleted(
        override val transactionId: String,
        val completedSteps: List<String>,
        val remainingSteps: List<String>,
        val requiresUserAction: Boolean = false
    ) : RollbackState()

    companion object {
        fun isTerminalState(state: RollbackState): Boolean = when (state) {
            is Completed, is Failed -> true
            else -> false
        }

        fun requiresUserAction(state: RollbackState): Boolean = when (state) {
            is RequiresManualIntervention -> true
            is PartiallyCompleted -> state.requiresUserAction
            else -> false
        }
    }
} 