package com.example.fideicomisoapproverring.transaction

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.stellar.sdk.responses.SubmitTransactionResponse
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages transaction state persistence and rollback capabilities
 */
class TransactionStateManager(private val context: Context) {
    companion object {
        private const val TAG = "TransactionState"
        private const val TRANSACTION_STATE_DIR = "transaction_states"
    }

    private val activeTransactions = ConcurrentHashMap<String, TransactionState>()

    data class TransactionState(
        val transactionId: String,
        val sourceAccount: String,
        val destinationAccount: String,
        val amount: String,
        val timestamp: Long = System.currentTimeMillis(),
        var status: TransactionStatus = TransactionStatus.INITIATED,
        var hash: String? = null,
        var rollbackHash: String? = null
    )

    enum class TransactionStatus {
        INITIATED,
        PENDING,
        COMPLETED,
        FAILED,
        ROLLING_BACK,
        ROLLED_BACK
    }

    /**
     * Saves the initial state of a transaction before execution
     */
    suspend fun saveTransactionState(
        transactionId: String,
        sourceAccount: String,
        destinationAccount: String,
        amount: String
    ) {
        val state = TransactionState(
            transactionId = transactionId,
            sourceAccount = sourceAccount,
            destinationAccount = destinationAccount,
            amount = amount
        )
        
        activeTransactions[transactionId] = state
        persistTransactionState(state)
        Log.d(TAG, "Saved initial state for transaction $transactionId")
    }

    /**
     * Updates the state of a transaction
     */
    suspend fun updateTransactionState(
        transactionId: String,
        status: TransactionStatus,
        response: SubmitTransactionResponse? = null
    ) {
        val state = activeTransactions[transactionId] ?: return
        state.status = status
        response?.let { state.hash = it.hash }
        
        activeTransactions[transactionId] = state
        persistTransactionState(state)
        Log.d(TAG, "Updated state for transaction $transactionId to $status")
    }

    /**
     * Initiates a rollback for a failed transaction
     */
    suspend fun initiateRollback(transactionId: String): Boolean {
        val state = activeTransactions[transactionId] ?: return false
        
        if (state.status != TransactionStatus.FAILED) {
            Log.w(TAG, "Cannot rollback transaction $transactionId in state ${state.status}")
            return false
        }

        state.status = TransactionStatus.ROLLING_BACK
        activeTransactions[transactionId] = state
        persistTransactionState(state)
        
        Log.d(TAG, "Initiated rollback for transaction $transactionId")
        return true
    }

    /**
     * Completes a rollback process
     */
    suspend fun completeRollback(transactionId: String, rollbackResponse: SubmitTransactionResponse) {
        val state = activeTransactions[transactionId] ?: return
        
        state.status = TransactionStatus.ROLLED_BACK
        state.rollbackHash = rollbackResponse.hash
        
        activeTransactions[transactionId] = state
        persistTransactionState(state)
        Log.d(TAG, "Completed rollback for transaction $transactionId")
    }

    /**
     * Retrieves the current state of a transaction
     */
    fun getTransactionState(transactionId: String): TransactionState? {
        return activeTransactions[transactionId]
    }

    /**
     * Persists transaction state to disk
     */
    private suspend fun persistTransactionState(state: TransactionState) {
        withContext(Dispatchers.IO) {
            try {
                val stateDir = File(context.filesDir, TRANSACTION_STATE_DIR)
                if (!stateDir.exists()) {
                    stateDir.mkdirs()
                }

                val stateFile = File(stateDir, "${state.transactionId}.json")
                val stateJson = JSONObject().apply {
                    put("transactionId", state.transactionId)
                    put("sourceAccount", state.sourceAccount)
                    put("destinationAccount", state.destinationAccount)
                    put("amount", state.amount)
                    put("timestamp", state.timestamp)
                    put("status", state.status.name)
                    put("hash", state.hash ?: "")
                    put("rollbackHash", state.rollbackHash ?: "")
                }

                stateFile.writeText(stateJson.toString())
                Log.d(TAG, "Persisted state for transaction ${state.transactionId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting transaction state: ${e.message}")
            }
        }
    }

    /**
     * Loads persisted transaction states
     */
    suspend fun loadPersistedStates() {
        withContext(Dispatchers.IO) {
            try {
                val stateDir = File(context.filesDir, TRANSACTION_STATE_DIR)
                if (!stateDir.exists()) return@withContext

                stateDir.listFiles()?.forEach { file ->
                    try {
                        val stateJson = JSONObject(file.readText())
                        val state = TransactionState(
                            transactionId = stateJson.getString("transactionId"),
                            sourceAccount = stateJson.getString("sourceAccount"),
                            destinationAccount = stateJson.getString("destinationAccount"),
                            amount = stateJson.getString("amount"),
                            timestamp = stateJson.getLong("timestamp"),
                            status = TransactionStatus.valueOf(stateJson.getString("status")),
                            hash = stateJson.getString("hash").takeIf { it.isNotEmpty() },
                            rollbackHash = stateJson.getString("rollbackHash").takeIf { it.isNotEmpty() }
                        )
                        activeTransactions[state.transactionId] = state
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading state from ${file.name}: ${e.message}")
                    }
                }
                Log.d(TAG, "Loaded ${activeTransactions.size} persisted transaction states")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading persisted states: ${e.message}")
            }
        }
    }

    /**
     * Cleans up completed transaction states older than the specified duration
     */
    suspend fun cleanupOldTransactions(maxAgeMs: Long = 24 * 60 * 60 * 1000) { // Default 24 hours
        withContext(Dispatchers.IO) {
            try {
                val stateDir = File(context.filesDir, TRANSACTION_STATE_DIR)
                if (!stateDir.exists()) return@withContext

                val currentTime = System.currentTimeMillis()
                stateDir.listFiles()?.forEach { file ->
                    try {
                        val stateJson = JSONObject(file.readText())
                        val timestamp = stateJson.getLong("timestamp")
                        val status = TransactionStatus.valueOf(stateJson.getString("status"))
                        
                        if (currentTime - timestamp > maxAgeMs && 
                            (status == TransactionStatus.COMPLETED || status == TransactionStatus.ROLLED_BACK)) {
                            file.delete()
                            val transactionId = stateJson.getString("transactionId")
                            activeTransactions.remove(transactionId)
                            Log.d(TAG, "Cleaned up old transaction $transactionId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error cleaning up ${file.name}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during cleanup: ${e.message}")
            }
        }
    }
} 