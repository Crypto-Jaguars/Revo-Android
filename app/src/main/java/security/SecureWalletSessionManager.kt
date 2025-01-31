package com.example.fideicomisoapproverring.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import androidx.security.crypto.MasterKeys.getOrCreate

class SecureWalletSessionManager(context: Context) {
    private val masterKeyAlias = getOrCreate(AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_wallet_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveWalletSession(walletName: String, sessionToken: String, deviceId: String) {
        val timestamp = System.currentTimeMillis().toString()

        sharedPreferences.edit().apply {
            putString("wallet_name", walletName)
            putString("session_token", sessionToken)
            putString("device_id", deviceId)
            putString("session_timestamp", timestamp)
            putString("wallet_address", "")
            putString("network", "public")
            apply()
        }

        printStoredData()
    }

    fun updateWalletAddress(publicKey: String) {
        sharedPreferences.edit().apply {
            putString("wallet_address", publicKey)
            apply()
        }
        printStoredData()
    }

    fun getWalletSession(): SessionData? {
        val walletName = sharedPreferences.getString("wallet_name", null)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val deviceId = sharedPreferences.getString("device_id", null)
        val timestamp = sharedPreferences.getString("session_timestamp", null)?.toLongOrNull()
        val walletAddress = sharedPreferences.getString("wallet_address", null)
        val network = sharedPreferences.getString("network", null)

        if (walletName != null && sessionToken != null && deviceId != null && timestamp != null) {
            if (System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000) {
                clearWalletSession()
                return null
            }
            return SessionData(
                walletName = walletName,
                sessionToken = sessionToken,
                deviceId = deviceId,
                timestamp = timestamp,
                walletAddress = walletAddress,
                network = network
            )
        }
        return null
    }

    fun clearWalletSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun printStoredData() {
        val session = getWalletSession()
        Log.d("WalletSession", """
            Stored Data:
            Wallet Name: ${session?.walletName}
            Wallet Address: ${session?.walletAddress}
            Session Token: ${session?.sessionToken}
            Device ID: ${session?.deviceId}
            Network: ${session?.network}
            Timestamp: ${session?.timestamp}
        """.trimIndent())
    }
}

data class SessionData(
    val walletName: String,
    val sessionToken: String,
    val deviceId: String,
    val timestamp: Long,
    val walletAddress: String? = null,
    val network: String? = null
)
