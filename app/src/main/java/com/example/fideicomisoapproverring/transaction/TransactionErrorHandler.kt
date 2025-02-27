package com.example.fideicomisoapproverring.transaction

import android.util.Log
import org.stellar.sdk.responses.SubmitTransactionResponse
import java.io.IOException

/**
 * Handles transaction errors and provides error detection, logging, and categorization.
 */
class TransactionErrorHandler {
    companion object {
        private const val TAG = "TransactionError"
        
        // Error categories
        enum class ErrorType {
            NETWORK,         // Network connectivity issues
            BLOCKCHAIN,      // Stellar blockchain specific errors
            WALLET,         // Wallet connection/signing issues
            CONTRACT,       // Smart contract/escrow issues
            UNKNOWN         // Unclassified errors
        }
    }

    /**
     * Represents a transaction error with all relevant details
     */
    data class TransactionError(
        val type: ErrorType,
        val code: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis(),
        val transactionId: String? = null,
        val recoverable: Boolean = false
    )

    /**
     * Detects and categorizes errors from various sources
     */
    fun detectError(throwable: Throwable, transactionId: String? = null): TransactionError {
        return when (throwable) {
            is IOException -> TransactionError(
                type = ErrorType.NETWORK,
                code = "NETWORK_ERROR",
                message = "Network connection error: ${throwable.message}",
                transactionId = transactionId,
                recoverable = true
            )
            else -> handleUnknownError(throwable, transactionId)
        }
    }

    /**
     * Handles Stellar-specific transaction errors
     */
    fun handleStellarError(response: SubmitTransactionResponse, transactionId: String? = null): TransactionError {
        if (response.isSuccess) {
            throw IllegalArgumentException("Transaction was successful, no error to handle")
        }

        val extras = response.extras
        return when {
            extras?.resultCodes?.transactionResultCode == "tx_insufficient_fee" -> TransactionError(
                type = ErrorType.BLOCKCHAIN,
                code = "INSUFFICIENT_FEE",
                message = "Transaction fee is too low",
                transactionId = transactionId,
                recoverable = true
            )
            extras?.resultCodes?.transactionResultCode == "tx_insufficient_balance" -> TransactionError(
                type = ErrorType.BLOCKCHAIN,
                code = "INSUFFICIENT_BALANCE",
                message = "Insufficient funds for transaction",
                transactionId = transactionId,
                recoverable = false
            )
            else -> TransactionError(
                type = ErrorType.BLOCKCHAIN,
                code = extras?.resultCodes?.transactionResultCode ?: "UNKNOWN_BLOCKCHAIN_ERROR",
                message = "Blockchain error: ${extras?.resultCodes?.transactionResultCode}",
                transactionId = transactionId,
                recoverable = false
            )
        }
    }

    /**
     * Handles wallet-related errors
     */
    fun handleWalletError(error: Exception, transactionId: String? = null): TransactionError {
        return TransactionError(
            type = ErrorType.WALLET,
            code = "WALLET_ERROR",
            message = "Wallet error: ${error.message}",
            transactionId = transactionId,
            recoverable = true
        )
    }

    /**
     * Handles smart contract/escrow errors
     */
    fun handleContractError(error: Exception, transactionId: String? = null): TransactionError {
        return TransactionError(
            type = ErrorType.CONTRACT,
            code = "CONTRACT_ERROR",
            message = "Smart contract error: ${error.message}",
            transactionId = transactionId,
            recoverable = false
        )
    }

    private fun handleUnknownError(throwable: Throwable, transactionId: String?): TransactionError {
        return TransactionError(
            type = ErrorType.UNKNOWN,
            code = "UNKNOWN_ERROR",
            message = "Unknown error: ${throwable.message}",
            transactionId = transactionId,
            recoverable = false
        )
    }

    /**
     * Logs transaction errors for monitoring and debugging
     */
    fun logError(error: TransactionError) {
        Log.e(TAG, """
            Transaction Error:
            Type: ${error.type}
            Code: ${error.code}
            Message: ${error.message}
            Transaction ID: ${error.transactionId ?: "N/A"}
            Timestamp: ${error.timestamp}
            Recoverable: ${error.recoverable}
        """.trimIndent())
    }
} 