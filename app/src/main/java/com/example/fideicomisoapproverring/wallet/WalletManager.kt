package com.example.fideicomisoapproverring.wallet

import com.example.fideicomisoapproverring.util.AppLogger
import org.stellar.sdk.KeyPair
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletManager @Inject constructor() {
    private var currentWallet: KeyPair? = null
    private var isConnected = false

    fun connect(secretKey: String): Boolean {
        return try {
            val keyPair = KeyPair.fromSecretSeed(secretKey)
            currentWallet = keyPair
            isConnected = true
            AppLogger.Wallet.info("Wallet connected successfully")
            true
        } catch (e: Exception) {
            AppLogger.Wallet.error("Failed to connect wallet", e)
            isConnected = false
            false
        }
    }

    fun disconnect() {
        currentWallet = null
        isConnected = false
        AppLogger.Wallet.info("Wallet disconnected")
    }

    fun isConnected(): Boolean {
        return isConnected && currentWallet != null
    }

    fun getCurrentWallet(): KeyPair? {
        return currentWallet
    }

    fun getPublicKey(): String? {
        return currentWallet?.accountId
    }

    fun sign(data: ByteArray): ByteArray? {
        return try {
            currentWallet?.sign(data)
        } catch (e: Exception) {
            AppLogger.Wallet.error("Failed to sign data", e)
            null
        }
    }
} 