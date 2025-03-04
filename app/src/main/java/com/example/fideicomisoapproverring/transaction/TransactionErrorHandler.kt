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
        val userMessage: String,
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
                userMessage = "We're having trouble connecting to the network. Please check your internet connection and try again.",
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
                userMessage = "The network is busy right now. We'll try again with a slightly higher fee.",
                transactionId = transactionId,
                recoverable = true
            )
            extras?.resultCodes?.transactionResultCode == "tx_insufficient_balance" -> TransactionError(
                type = ErrorType.BLOCKCHAIN,
                code = "INSUFFICIENT_BALANCE",
                message = "Insufficient funds for transaction",
                userMessage = "There aren't enough funds in your account to complete this transaction. Please check your balance.",
                transactionId = transactionId,
                recoverable = false
            )
            else -> TransactionError(
                type = ErrorType.BLOCKCHAIN,
                code = extras?.resultCodes?.transactionResultCode ?: "UNKNOWN_BLOCKCHAIN_ERROR",
                message = "Blockchain error: ${extras?.resultCodes?.transactionResultCode}",
                userMessage = "There was an issue processing your transaction. Our team has been notified and will help resolve this.",
                transactionId = transactionId,
                recoverable = false
            )
        }
    }

    /**
     * Handles wallet-related errors
     */
    fun handleWalletError(error: Exception, transactionId: String? = null): TransactionError {
        return when {
            error.message?.contains("base32") == true -> TransactionError(
                type = ErrorType.WALLET,
                code = "WALLET_FORMAT_ERROR",
                message = "Invalid wallet address format",
                userMessage = "There seems to be an issue with your wallet address. Please try reconnecting your wallet.",
                transactionId = transactionId,
                recoverable = true
            )
            error.message?.contains("connection") == true -> TransactionError(
                type = ErrorType.WALLET,
                code = "WALLET_CONNECTION_ERROR",
                message = "Wallet connection error: ${error.message}",
                userMessage = "We lost connection to your wallet. Please check that it's still connected and try again.",
                transactionId = transactionId,
                recoverable = true
            )
            else -> TransactionError(
                type = ErrorType.WALLET,
                code = "WALLET_ERROR",
                message = "Wallet error: ${error.message}",
                userMessage = "There was an issue with your wallet. Please try disconnecting and connecting again.",
                transactionId = transactionId,
                recoverable = true
            )
        }
    }

    /**
     * Handles smart contract/escrow errors
     */
    fun handleContractError(error: Exception, transactionId: String? = null): TransactionError {
        return TransactionError(
            type = ErrorType.CONTRACT,
            code = "CONTRACT_ERROR",
            message = "Smart contract error: ${error.message}",
            userMessage = "There was an issue with the escrow contract. Our support team will help you resolve this.",
            transactionId = transactionId,
            recoverable = false
        )
    }

    private fun handleUnknownError(throwable: Throwable, transactionId: String?): TransactionError {
        return TransactionError(
            type = ErrorType.UNKNOWN,
            code = "UNKNOWN_ERROR",
            message = "Unknown error: ${throwable.message}",
            userMessage = "Something unexpected happened. We're looking into it and will help you resolve this issue.",
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
            Technical Message: ${error.message}
            User Message: ${error.userMessage}
            Transaction ID: ${error.transactionId ?: "N/A"}
            Timestamp: ${error.timestamp}
            Recoverable: ${error.recoverable}
        """.trimIndent())
    }
} 