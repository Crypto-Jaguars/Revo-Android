package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents the possible states of a transaction in the Revolutionary Farmers marketplace.
 * This sealed class hierarchy provides a type-safe way to handle transaction state transitions.
 */
sealed class TransactionState {
    abstract val transactionId: String
    abstract val timestamp: Long

    /**
     * Initial state when a transaction is first created
     */
    data class Initial(
        override val transactionId: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * State when a transaction is being processed
     */
    data class Processing(
        override val transactionId: String,
        val progress: Int,
        val details: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * State when a transaction is being verified
     */
    data class Verifying(
        override val transactionId: String,
        val stage: VerificationStage,
        val progress: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * State when a transaction has been confirmed
     */
    data class Confirmed(
        override val transactionId: String,
        val blockchainHash: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * State when a transaction has encountered an error
     */
    data class Error(
        override val transactionId: String,
        val error: TransactionError,
        val recoverable: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * State when a rollback has been initiated for a transaction
     */
    data class RollbackInitiated(
        override val transactionId: String,
        val reason: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * State when a rollback is in progress
     */
    data class RollbackInProgress(
        override val transactionId: String,
        val progress: Int,
        val details: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * Terminal state for a transaction (success, failed, or rolled back)
     */
    data class Terminal(
        override val transactionId: String,
        val outcome: TransactionOutcome,
        val finalStatus: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : TransactionState()

    /**
     * Represents the verification stages a transaction goes through
     */
    enum class VerificationStage {
        BASIC_VALIDATION,
        BLOCKCHAIN_STATE,
        SMART_CONTRACT,
        ESCROW_STATE,
        FINAL_CONSISTENCY
    }

    /**
     * Represents the final outcome of a transaction
     */
    enum class TransactionOutcome {
        SUCCESS,
        FAILED,
        ROLLED_BACK
    }

    companion object {
        fun isTerminal(state: TransactionState): Boolean {
            return state is Terminal
        }

        fun requiresAction(state: TransactionState): Boolean {
            return state is Error && state.recoverable
        }

        fun canTransition(from: TransactionState, to: TransactionState): Boolean {
            return when (from) {
                is Initial -> to is Processing || to is Error
                is Processing -> to is Verifying || to is Error
                is Verifying -> to is Confirmed || to is Error
                is Error -> to is Processing || to is RollbackInitiated || to is Terminal
                is RollbackInitiated -> to is RollbackInProgress || to is Terminal
                is RollbackInProgress -> to is Terminal
                is Confirmed -> to is Terminal
                is Terminal -> false
            }
        }
    }
} 