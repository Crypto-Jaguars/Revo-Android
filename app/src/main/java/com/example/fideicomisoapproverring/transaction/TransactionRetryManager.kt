package com.example.fideicomisoapproverring.transaction

import android.util.Log
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

/**
 * Manages automatic retry attempts for recoverable transaction errors
 */
class TransactionRetryManager {
    companion object {
        private const val TAG = "TransactionRetry"
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 1000L // 1 second
    }

    private val retryAttempts = ConcurrentHashMap<String, Int>()
    private val transactionStates = ConcurrentHashMap<String, TransactionState>()

    data class TransactionState(
        val transactionId: String,
        var attempts: Int = 0,
        var lastError: TransactionErrorHandler.TransactionError? = null,
        var lastAttemptTime: Long = System.currentTimeMillis()
    )

    /**
     * Determines if a transaction error is eligible for retry
     */
    fun isRetryable(error: TransactionErrorHandler.TransactionError): Boolean {
        return when (error.type) {
            TransactionErrorHandler.Companion.ErrorType.NETWORK -> true
            TransactionErrorHandler.Companion.ErrorType.BLOCKCHAIN -> isRetryableBlockchainError(error)
            TransactionErrorHandler.Companion.ErrorType.WALLET -> isRetryableWalletError(error)
            else -> false
        }
    }

    private fun isRetryableBlockchainError(error: TransactionErrorHandler.TransactionError): Boolean {
        return when (error.code) {
            "tx_insufficient_fee" -> true
            "tx_bad_seq" -> true
            "tx_failed" -> false // Generic failure, not retryable
            else -> false
        }
    }

    private fun isRetryableWalletError(error: TransactionErrorHandler.TransactionError): Boolean {
        return when (error.code) {
            "WALLET_CONNECTION_ERROR" -> true
            "SIGNING_TIMEOUT" -> true
            else -> false
        }
    }

    /**
     * Calculates the delay before the next retry attempt using exponential backoff
     */
    private fun calculateRetryDelay(attempts: Int): Long {
        return BASE_DELAY_MS * (2.0.pow(attempts.toDouble())).toLong()
    }

    /**
     * Handles a transaction error and determines if/when to retry
     */
    suspend fun handleTransactionError(
        transactionId: String,
        error: TransactionErrorHandler.TransactionError,
        retryAction: suspend () -> Unit
    ) {
        val state = transactionStates.getOrPut(transactionId) { 
            TransactionState(transactionId) 
        }

        if (!isRetryable(error) || state.attempts >= MAX_RETRIES) {
            Log.d(TAG, "Transaction $transactionId not retryable or max retries reached")
            return
        }

        state.attempts++
        state.lastError = error
        state.lastAttemptTime = System.currentTimeMillis()
        transactionStates[transactionId] = state

        val retryDelay = calculateRetryDelay(state.attempts)
        Log.d(TAG, "Retrying transaction $transactionId after ${retryDelay}ms (Attempt ${state.attempts})")
        
        delay(retryDelay)
        retryAction()
    }

    /**
     * Resets retry state for a transaction
     */
    fun resetRetryState(transactionId: String) {
        transactionStates.remove(transactionId)
        retryAttempts.remove(transactionId)
        Log.d(TAG, "Reset retry state for transaction $transactionId")
    }

    /**
     * Gets the current retry state for a transaction
     */
    fun getRetryState(transactionId: String): TransactionState? {
        return transactionStates[transactionId]
    }

    /**
     * Logs retry attempt details
     */
    private fun logRetryAttempt(transactionId: String, attempt: Int, error: TransactionErrorHandler.TransactionError) {
        Log.d(TAG, """
            Retry Attempt for Transaction:
            ID: $transactionId
            Attempt: $attempt of $MAX_RETRIES
            Error Type: ${error.type}
            Error Code: ${error.code}
            Error Message: ${error.message}
        """.trimIndent())
    }
} 