package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents the state of a transaction verification process.
 * This sealed class hierarchy provides comprehensive tracking of verification progress.
 */
sealed class VerificationState {
    abstract val transactionId: String

    data class Started(
        override val transactionId: String
    ) : VerificationState()

    data class InProgress(
        override val transactionId: String,
        val stage: String,
        val progress: Int
    ) : VerificationState()

    data class Failed(
        override val transactionId: String,
        val reason: String,
        val details: String
    ) : VerificationState()

    data class Completed(
        override val transactionId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : VerificationState()

    companion object {
        fun isTerminalState(state: VerificationState): Boolean = when (state) {
            is Completed, is Failed -> true
            else -> false
        }

        fun getProgress(state: VerificationState): Int = when (state) {
            is Started -> 0
            is InProgress -> state.progress
            is Failed -> 100
            is Completed -> 100
        }
    }
} 