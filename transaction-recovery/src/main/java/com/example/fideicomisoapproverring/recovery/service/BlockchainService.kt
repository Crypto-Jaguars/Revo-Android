package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.recovery.model.BlockchainEvent
import com.example.fideicomisoapproverring.recovery.model.TransactionError
import com.example.fideicomisoapproverring.recovery.forensics.BlockchainState
import com.example.fideicomisoapproverring.stellar.model.NetworkState
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for blockchain interactions in the Revolutionary Farmers marketplace.
 * This service handles blockchain-specific operations and state management.
 */
interface BlockchainService {
    /**
     * Retrieves error information for a specific transaction
     * @param transactionId The ID of the transaction to check
     * @return TransactionError if an error exists, null otherwise
     */
    suspend fun getTransactionError(transactionId: String): TransactionError?

    /**
     * Gets the current state of the blockchain
     * @return Current blockchain state including height, block time, and gas prices
     */
    suspend fun getBlockchainState(): BlockchainState

    /**
     * Gets the current network state including congestion levels
     * @return Current network state
     */
    suspend fun getNetworkState(): NetworkState

    /**
     * Verifies if a transaction can be safely retried
     * @param transactionId The ID of the transaction to check
     * @return true if the transaction can be retried, false otherwise
     */
    suspend fun canRetryTransaction(transactionId: String): Boolean

    /**
     * Estimates the gas price for a transaction retry
     * @param transactionId The ID of the transaction to estimate for
     * @return Estimated gas price in Stellar stroops
     */
    suspend fun estimateRetryGasPrice(transactionId: String): Long

    /**
     * Checks if a transaction has been finalized on the blockchain
     * @param transactionId The ID of the transaction to check
     * @return true if the transaction is finalized, false otherwise
     */
    suspend fun isTransactionFinalized(transactionId: String): Boolean

    /**
     * Gets the number of confirmations for a transaction
     * @param transactionId The ID of the transaction to check
     * @return Number of confirmations, null if transaction not found
     */
    suspend fun getTransactionConfirmations(transactionId: String): Int?

    /**
     * Monitors the blockchain for specific events related to a transaction
     * @param transactionId The ID of the transaction to monitor
     * @return Flow of blockchain events related to the transaction
     */
    fun monitorTransactionEvents(transactionId: String): Flow<BlockchainEvent>
} 