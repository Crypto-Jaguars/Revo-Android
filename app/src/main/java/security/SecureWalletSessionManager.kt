package com.example.fideicomisoapproverring.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import androidx.security.crypto.MasterKeys.getOrCreate
import java.util.Base64

class SecureWalletSessionManager(context: Context) {
    private val TAG = "SecureWallet"
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
            apply()
        }

        Log.d(TAG, "💾 Saved new wallet session:")

    }

    fun updateWalletAddress(publicKey: String): Boolean {
        if (!isValidStellarPublicKey(publicKey)) {
            Log.e(TAG, "❌ Invalid Stellar public key format")
            return false
        }

        sharedPreferences.edit().apply {
            putString("wallet_address", publicKey)
            apply()
        }

        Log.d(TAG, "✅ Updated wallet address")

        return true
    }

    fun getWalletSession(): SessionData? {
        val walletName = sharedPreferences.getString("wallet_name", null)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val deviceId = sharedPreferences.getString("device_id", null)
        val timestamp = sharedPreferences.getString("session_timestamp", null)?.toLongOrNull()
        val walletAddress = sharedPreferences.getString("wallet_address", null)

        if (walletName != null && sessionToken != null && deviceId != null && timestamp != null) {
            if (System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000) {
                Log.d(TAG, "⏰ Session expired, clearing data")
                clearWalletSession()
                return null
            }
            return SessionData(
                walletName = walletName,
                sessionToken = sessionToken,
                deviceId = deviceId,
                timestamp = timestamp,
                walletAddress = walletAddress
            )
        }
        Log.d(TAG, "❌ No valid session found")
        return null
    }

    fun clearWalletSession() {
        Log.d(TAG, "🗑️ Clearing wallet session")
        sharedPreferences.edit().clear().apply()
    }



    private fun isValidStellarPublicKey(publicKey: String): Boolean {
        return try {

            if (!publicKey.startsWith("G")) {
                return false
            }
            if (publicKey.length != 56) {
                return false
            }

            val base32Regex = "^[A-Z2-7]+$".toRegex()
            if (!base32Regex.matches(publicKey.substring(1))) {
                return false
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validating public key: ${e.message}")
            false
        }
    }
}

data class SessionData(
    val walletName: String,
    val sessionToken: String,
    val deviceId: String,
    val timestamp: Long,
    val walletAddress: String? = null
)
