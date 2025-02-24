package com.example.fideicomisoapproverring.core.wallet

import com.example.fideicomisoapproverring.core.model.TransactionError
import kotlinx.coroutines.flow.Flow

interface WalletManager {
    /**
     * Get the current wallet address
     * @return The wallet address as a string
     */
    suspend fun getWalletAddress(): String

    /**
     * Get the current balance of the wallet
     * @return The balance as a BigDecimal
     */
    suspend fun getBalance(): String

    /**
     * Sign a transaction with the wallet's private key
     * @param transactionData The transaction data to sign
     * @return The signed transaction data
     */
    suspend fun signTransaction(transactionData: String): String

    /**
     * Verify if a transaction was signed by this wallet
     * @param transactionData The transaction data
     * @param signature The signature to verify
     * @return true if the signature is valid, false otherwise
     */
    suspend fun verifySignature(transactionData: String, signature: String): Boolean

    /**
     * Get the transaction history for this wallet
     * @param limit Optional limit on the number of transactions to return
     * @return A flow of transaction data
     */
    fun getTransactionHistory(limit: Int? = null): Flow<List<String>>

    /**
     * Check if the wallet has sufficient funds for a transaction
     * @param amount The amount to check
     * @return true if sufficient funds are available, false otherwise
     */
    suspend fun hasSufficientFunds(amount: String): Boolean

    /**
     * Lock the wallet to prevent unauthorized access
     */
    suspend fun lock()

    /**
     * Unlock the wallet for use
     * @param password The password to unlock the wallet
     * @return true if successfully unlocked, false otherwise
     */
    suspend fun unlock(password: String): Boolean

    /**
     * Check if the wallet is currently locked
     * @return true if locked, false if unlocked
     */
    fun isLocked(): Boolean

    /**
     * Export the wallet's encrypted backup
     * @param password The password to encrypt the backup
     * @return The encrypted backup data
     */
    suspend fun exportBackup(password: String): String

    /**
     * Import a wallet from an encrypted backup
     * @param backupData The encrypted backup data
     * @param password The password to decrypt the backup
     * @return true if import was successful, false otherwise
     */
    suspend fun importBackup(backupData: String, password: String): Boolean

    /**
     * Get the current status of the wallet
     * @return A flow of wallet status updates
     */
    fun getWalletStatus(): Flow<WalletStatus>
}

enum class WalletStatus {
    READY,
    LOCKED,
    SYNCING,
    ERROR
} 