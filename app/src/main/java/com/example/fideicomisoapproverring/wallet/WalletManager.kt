package com.example.fideicomisoapproverring.wallet

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.stellar.sdk.Account
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Network
import org.stellar.sdk.Transaction
import org.stellar.sdk.ManageDataOperation
import org.stellar.sdk.Server

class WalletManager(private val repository: WalletRepository) {
    private val _walletState = MutableStateFlow<WalletState>(WalletState.Disconnected)
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val CONNECTION_TIMEOUT = 30000L // 30 seconds

    suspend fun connectWallet(walletType: String) {
        try {
            _walletState.value = WalletState.Connecting
            
            withTimeout(CONNECTION_TIMEOUT) {
                // Attempt connection based on wallet type
                val result = withContext(Dispatchers.IO) {
                    when (walletType) {
                        "LOBSTR" -> connectLobstr()
                        else -> throw IllegalArgumentException("Unsupported wallet type")
                    }
                }
                
                if (result.isSuccess) {
                    val publicKey = result.getOrNull()!!
                    repository.saveWalletConnection(publicKey)
                    _walletState.value = WalletState.Connected(publicKey)
                } else {
                    _walletState.value = WalletState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            _walletState.value = WalletState.Error(e.message ?: "Connection failed")
        }
    }

    private suspend fun connectLobstr(): Result<String> {
        return try {
            // Create a challenge transaction
            val server = Server("https://horizon-testnet.stellar.org")
            val sourceKeyPair = KeyPair.random()
            
            // Build the challenge transaction
            val transaction = Transaction.Builder(
                Account(sourceKeyPair.accountId, -1L),
                Network.TESTNET
            )
                .addOperation(ManageDataOperation.Builder(
                    "auth", 
                    "challenge".toByteArray()
                ).build())
                .setTimeout(300)
                .setBaseFee(100)
                .build()

            // Convert transaction to XDR format
            val challengeTx = transaction.toEnvelopeXdrBase64()

            // Store the challenge transaction for verification
            repository.saveWalletConnection(sourceKeyPair.accountId)
            
            Result.success(sourceKeyPair.accountId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnectWallet() {
        repository.clearWalletConnection()
        _walletState.value = WalletState.Disconnected
    }
}