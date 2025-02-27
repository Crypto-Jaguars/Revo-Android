package com.example.fideicomisoapproverring.transaction

import android.util.Log
import org.stellar.sdk.responses.SubmitTransactionResponse
import java.util.concurrent.ConcurrentHashMap

/**
 * Monitors transaction flows and tracks their status
 */
class TransactionMonitor {
    companion object {
        private const val TAG = "TransactionMonitor"
    }

    private val errorHandler = TransactionErrorHandler()
    private val transactionStates = ConcurrentHashMap<String, TransactionState>()

    enum class TransactionStatus {
        INITIATED,
        WALLET_CONNECTED,
        SIGNING,
        SIGNED,
        SUBMITTING,
        COMPLETED,
        FAILED
    }

    data class TransactionState(
        val transactionId: String,
        var status: TransactionStatus,
        val startTime: Long = System.currentTimeMillis(),
        var error: TransactionErrorHandler.TransactionError? = null
    )

    /**
     * Start monitoring a new transaction
     */
    fun startTransaction(transactionId: String): TransactionState {
        val state = TransactionState(transactionId, TransactionStatus.INITIATED)
        transactionStates[transactionId] = state
        logTransactionEvent(transactionId, "Transaction initiated")
        return state
    }

    /**
     * Update transaction status during wallet connection
     */
    fun onWalletConnection(transactionId: String, success: Boolean, error: Exception? = null) {
        val state = getTransactionState(transactionId)
        if (success) {
            state.status = TransactionStatus.WALLET_CONNECTED
            logTransactionEvent(transactionId, "Wallet connected successfully")
        } else {
            state.status = TransactionStatus.FAILED
            state.error = error?.let { errorHandler.handleWalletError(it, transactionId) }
            state.error?.let { errorHandler.logError(it) }
        }
        transactionStates[transactionId] = state
    }

    /**
     * Monitor transaction signing process
     */
    fun onTransactionSigning(transactionId: String) {
        updateTransactionStatus(transactionId, TransactionStatus.SIGNING)
        logTransactionEvent(transactionId, "Transaction signing started")
    }

    /**
     * Handle completion of signing process
     */
    fun onTransactionSigned(transactionId: String, success: Boolean, error: Exception? = null) {
        val state = getTransactionState(transactionId)
        if (success) {
            state.status = TransactionStatus.SIGNED
            logTransactionEvent(transactionId, "Transaction signed successfully")
        } else {
            state.status = TransactionStatus.FAILED
            state.error = error?.let { errorHandler.handleWalletError(it, transactionId) }
            state.error?.let { errorHandler.logError(it) }
        }
        transactionStates[transactionId] = state
    }

    /**
     * Monitor transaction submission to blockchain
     */
    fun onTransactionSubmission(transactionId: String, response: SubmitTransactionResponse) {
        val state = getTransactionState(transactionId)
        if (response.isSuccess) {
            state.status = TransactionStatus.COMPLETED
            logTransactionEvent(transactionId, "Transaction completed successfully")
        } else {
            state.status = TransactionStatus.FAILED
            state.error = errorHandler.handleStellarError(response, transactionId)
            state.error?.let { errorHandler.logError(it) }
        }
        transactionStates[transactionId] = state
    }

    /**
     * Handle contract execution monitoring
     */
    fun onContractExecution(transactionId: String, success: Boolean, error: Exception? = null) {
        val state = getTransactionState(transactionId)
        if (success) {
            logTransactionEvent(transactionId, "Contract executed successfully")
        } else {
            state.status = TransactionStatus.FAILED
            state.error = error?.let { errorHandler.handleContractError(it, transactionId) }
            state.error?.let { errorHandler.logError(it) }
        }
        transactionStates[transactionId] = state
    }

    /**
     * Get current state of a transaction
     */
    fun getTransactionState(transactionId: String): TransactionState {
        return transactionStates[transactionId] ?: throw IllegalStateException("Transaction $transactionId not found")
    }

    private fun updateTransactionStatus(transactionId: String, status: TransactionStatus) {
        val state = getTransactionState(transactionId)
        state.status = status
        transactionStates[transactionId] = state
    }

    private fun logTransactionEvent(transactionId: String, event: String) {
        Log.d(TAG, "Transaction $transactionId: $event")
    }
} 