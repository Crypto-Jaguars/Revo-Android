package com.example.fideicomisoapproverring.recovery.state

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages transaction states securely with atomic operations and data consistency guarantees.
 * This class is responsible for:
 * 1. Maintaining transaction state integrity
 * 2. Providing atomic state transitions
 * 3. Ensuring data consistency during recovery
 * 4. Implementing secure state persistence
 * 5. Handling concurrent state modifications safely
 */
@Singleton
class SecureTransactionStateManager @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val auditLogger: SecureAuditLogger
) {
    private val stateMutex = Mutex()
    private val transactionStates = ConcurrentHashMap<String, TransactionState>()
    
    private val _stateFlow = MutableStateFlow<Map<String, TransactionState>>(emptyMap())
    val stateFlow: StateFlow<Map<String, TransactionState>> = _stateFlow.asStateFlow()

    /**
     * Updates the state of a transaction atomically with audit logging
     */
    suspend fun updateTransactionState(
        transactionId: String,
        newState: TransactionState,
        metadata: Map<String, String> = emptyMap()
    ) {
        stateMutex.withLock {
            try {
                val oldState = transactionStates[transactionId]
                validateStateTransition(oldState, newState)
                
                transactionStates[transactionId] = newState
                _stateFlow.value = transactionStates.toMap()

                auditLogger.logEvent(
                    "TRANSACTION_STATE_UPDATED",
                    "Transaction state updated: $transactionId",
                    mapOf(
                        "old_state" to (oldState?.javaClass?.simpleName ?: "null"),
                        "new_state" to newState.javaClass.simpleName,
                        "timestamp" to System.currentTimeMillis().toString()
                    ) + metadata
                )
            } catch (e: Exception) {
                auditLogger.logEvent(
                    "STATE_UPDATE_FAILED",
                    "Failed to update transaction state: $transactionId",
                    mapOf(
                        "error" to e.message.toString(),
                        "timestamp" to System.currentTimeMillis().toString()
                    )
                )
                throw e
            }
        }
    }

    /**
     * Validates if a state transition is allowed based on the current state machine rules
     */
    private fun validateStateTransition(currentState: TransactionState?, newState: TransactionState) {
        when {
            currentState == null -> {
                require(newState is TransactionState.Initial) {
                    "First state must be Initial, got ${newState.javaClass.simpleName}"
                }
            }
            currentState is TransactionState.Terminal -> {
                throw IllegalStateException("Cannot transition from terminal state: ${currentState.javaClass.simpleName}")
            }
            !isValidTransition(currentState, newState) -> {
                throw IllegalStateException(
                    "Invalid state transition from ${currentState.javaClass.simpleName} to ${newState.javaClass.simpleName}"
                )
            }
        }
    }

    /**
     * Checks if a state transition is valid according to the state machine rules
     */
    private fun isValidTransition(from: TransactionState, to: TransactionState): Boolean {
        return when (from) {
            is TransactionState.Initial -> {
                to is TransactionState.Processing ||
                to is TransactionState.Error
            }
            is TransactionState.Processing -> {
                to is TransactionState.Verifying ||
                to is TransactionState.Error
            }
            is TransactionState.Verifying -> {
                to is TransactionState.Confirmed ||
                to is TransactionState.Error
            }
            is TransactionState.Error -> {
                to is TransactionState.Processing ||
                to is TransactionState.RollbackInitiated ||
                to is TransactionState.Terminal
            }
            is TransactionState.RollbackInitiated -> {
                to is TransactionState.RollbackInProgress ||
                to is TransactionState.Terminal
            }
            is TransactionState.RollbackInProgress -> {
                to is TransactionState.Terminal
            }
            is TransactionState.Confirmed -> {
                to is TransactionState.Terminal
            }
            is TransactionState.Terminal -> false
        }
    }

    /**
     * Retrieves the current state of a transaction
     */
    fun getTransactionState(transactionId: String): TransactionState? {
        return transactionStates[transactionId]
    }

    /**
     * Checks if a transaction is in a terminal state
     */
    fun isTransactionTerminal(transactionId: String): Boolean {
        return transactionStates[transactionId] is TransactionState.Terminal
    }

    /**
     * Retrieves all transactions in a specific state
     */
    fun getTransactionsInState(state: Class<out TransactionState>): List<String> {
        return transactionStates.entries
            .filter { state.isInstance(it.value) }
            .map { it.key }
    }

    /**
     * Cleans up completed transaction states that are older than the specified duration
     */
    suspend fun cleanupOldTransactions(maxAgeMillis: Long) {
        stateMutex.withLock {
            val now = System.currentTimeMillis()
            val oldTransactions = transactionStates.entries
                .filter { (_, state) ->
                    state is TransactionState.Terminal && 
                    (now - state.timestamp) > maxAgeMillis
                }
                .map { it.key }

            oldTransactions.forEach { transactionId ->
                transactionStates.remove(transactionId)
                auditLogger.logEvent(
                    "TRANSACTION_STATE_CLEANED",
                    "Cleaned up old transaction state: $transactionId",
                    mapOf("timestamp" to now.toString())
                )
            }

            if (oldTransactions.isNotEmpty()) {
                _stateFlow.value = transactionStates.toMap()
            }
        }
    }
} 