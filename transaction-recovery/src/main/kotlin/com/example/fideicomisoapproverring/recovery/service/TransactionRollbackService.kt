package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.core.wallet.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.stellar.sdk.responses.TransactionResponse
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRollbackService @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val walletManager: WalletManager,
    private val auditLogger: SecureAuditLogger
) {
    private val _rollbackState = MutableStateFlow<Map<String, RollbackState>>(emptyMap())
    val rollbackState: StateFlow<Map<String, RollbackState>> = _rollbackState.asStateFlow()

    suspend fun initiateRollback(transactionId: String, error: TransactionError) {
        try {
            auditLogger.logEvent(
                "ROLLBACK_INITIATED",
                "Initiating rollback for transaction: $transactionId",
                mapOf(
                    "error_type" to error::class.simpleName,
                    "error_message" to error.message
                )
            )
            updateState(transactionId, RollbackState.Analyzing(transactionId))

            val transaction = stellarTransactionManager.getTransaction(transactionId)
            when (error) {
                is InsufficientFundsError -> handleInsufficientFundsRollback(transaction)
                is SmartContractError -> handleSmartContractRollback(transaction)
                is EscrowError -> handleEscrowRollback(transaction)
                is NetworkCongestionError -> handleNetworkCongestionRollback(transaction)
                is WalletConnectionError -> handleWalletConnectionRollback(transaction)
                is BlockchainError -> handleBlockchainRollback(transaction)
                is TimeoutError -> handleTimeoutRollback(transaction)
                is UnknownError -> handleUnknownErrorRollback(transaction)
            }
        } catch (e: Exception) {
            auditLogger.logEvent(
                "ROLLBACK_FAILED",
                "Rollback failed for transaction: $transactionId",
                mapOf("error" to e.message.toString())
            )
            updateState(transactionId, RollbackState.Failed(transactionId, e.message ?: "Unknown error"))
            throw e
        }
    }

    private suspend fun handleInsufficientFundsRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        updateState(transactionId, RollbackState.InProgress(transactionId, "Initiating refund", 25))

        val refundTx = stellarTransactionManager.submitTransaction(
            sourceAccount = transaction.destinationAccount,
            destinationAccount = transaction.sourceAccount,
            amount = transaction.amount,
            memo = "Refund for failed transaction: $transactionId"
        )

        updateState(transactionId, RollbackState.Completed(transactionId, refundTx.hash))
        auditLogger.logEvent(
            "REFUND_COMPLETED",
            "Refund completed for transaction: $transactionId",
            mapOf("refund_transaction" to refundTx.hash)
        )
    }

    private suspend fun handleEscrowRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        updateState(transactionId, RollbackState.InProgress(transactionId, "Releasing escrow", 25))

        val escrowTx = stellarTransactionManager.releaseEscrow(
            escrowAccount = transaction.sourceAccount,
            beneficiary = transaction.destinationAccount,
            amount = transaction.amount
        )

        updateState(transactionId, RollbackState.Completed(transactionId, escrowTx.hash))
        auditLogger.logEvent(
            "ESCROW_RELEASED",
            "Escrow released for transaction: $transactionId",
            mapOf("release_transaction" to escrowTx.hash)
        )
    }

    private suspend fun handleSmartContractRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        updateState(transactionId, RollbackState.InProgress(transactionId, "Reversing smart contract", 25))

        val reversalTx = stellarTransactionManager.reverseSmartContract(
            contractAddress = transaction.sourceAccount,
            transactionId = transactionId
        )

        updateState(transactionId, RollbackState.Completed(transactionId, reversalTx.hash))
        auditLogger.logEvent(
            "CONTRACT_REVERSED",
            "Smart contract reversed for transaction: $transactionId",
            mapOf("reversal_transaction" to reversalTx.hash)
        )
    }

    private suspend fun handleNetworkCongestionRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        updateState(transactionId, RollbackState.InProgress(transactionId, "Processing partial refund", 25))

        // Apply a small fee for network congestion cases
        val refundAmount = BigDecimal(transaction.amount) * BigDecimal("0.999") // 0.1% fee
        val refundTx = stellarTransactionManager.submitTransaction(
            sourceAccount = transaction.destinationAccount,
            destinationAccount = transaction.sourceAccount,
            amount = refundAmount.toString(),
            memo = "Partial refund (network congestion): $transactionId"
        )

        updateState(transactionId, RollbackState.Completed(transactionId, refundTx.hash))
        auditLogger.logEvent(
            "PARTIAL_REFUND_COMPLETED",
            "Partial refund completed for transaction: $transactionId",
            mapOf("refund_transaction" to refundTx.hash)
        )
    }

    private suspend fun handleWalletConnectionRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        if (!walletManager.isConnected()) {
            updateState(
                transactionId,
                RollbackState.RequiresManualIntervention(
                    transactionId,
                    "Wallet connection required for rollback",
                    "Please reconnect your wallet to complete the rollback"
                )
            )
            return
        }

        handleInsufficientFundsRollback(transaction) // Use same logic as refund
    }

    private suspend fun handleBlockchainRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        updateState(transactionId, RollbackState.InProgress(transactionId, "Analyzing blockchain state", 25))

        try {
            val blockchainState = stellarTransactionManager.getBlockchainState()
            if (blockchainState.isHealthy) {
                handleInsufficientFundsRollback(transaction)
            } else {
                updateState(
                    transactionId,
                    RollbackState.RequiresManualIntervention(
                        transactionId,
                        "Blockchain instability detected",
                        "Please wait for network stability before retrying"
                    )
                )
            }
        } catch (e: Exception) {
            updateState(
                transactionId,
                RollbackState.Failed(transactionId, "Failed to analyze blockchain state: ${e.message}")
            )
            throw e
        }
    }

    private suspend fun handleTimeoutRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        updateState(transactionId, RollbackState.InProgress(transactionId, "Verifying transaction timeout", 25))

        if (isTransactionStillPending(transaction)) {
            handleInsufficientFundsRollback(transaction)
        } else {
            updateState(
                transactionId,
                RollbackState.RequiresManualIntervention(
                    transactionId,
                    "Transaction status unclear",
                    "Please verify transaction status before proceeding"
                )
            )
        }
    }

    private suspend fun handleUnknownErrorRollback(transaction: TransactionResponse) {
        val transactionId = transaction.hash
        updateState(
            transactionId,
            RollbackState.RequiresManualIntervention(
                transactionId,
                "Unknown error requires manual review",
                "Please contact support for assistance"
            )
        )
        auditLogger.logEvent(
            "MANUAL_REVIEW_REQUIRED",
            "Unknown error requires manual review for transaction: $transactionId",
            mapOf("transaction_details" to transaction.toString())
        )
    }

    private suspend fun isTransactionStillPending(transaction: TransactionResponse): Boolean {
        return try {
            val currentStatus = stellarTransactionManager.getTransaction(transaction.hash)
            currentStatus.isPending()
        } catch (e: Exception) {
            false
        }
    }

    private fun updateState(transactionId: String, state: RollbackState) {
        _rollbackState.value = _rollbackState.value + (transactionId to state)
        auditLogger.logEvent(
            "ROLLBACK_STATE_UPDATED",
            "Rollback state updated for transaction: $transactionId",
            mapOf(
                "state" to state::class.simpleName,
                "details" to state.toString()
            )
        )
    }
} 