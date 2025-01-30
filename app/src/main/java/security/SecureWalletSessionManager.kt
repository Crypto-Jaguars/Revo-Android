package com.example.fideicomisoapproverring.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import androidx.security.crypto.MasterKeys.getOrCreate
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureWalletSessionManager(context: Context) {
    private val masterKeyAlias = getOrCreate(AES256_GCM_SPEC)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_wallet_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveWalletSession(walletAddress: String, sessionToken: String, deviceId: String) {
        val timestamp = System.currentTimeMillis().toString()

        sharedPreferences.edit().apply {
            putString("wallet_address", walletAddress)
            putString("session_token", sessionToken)
            putString("device_id", deviceId)
            putString("session_timestamp", timestamp)
            apply()
        }
    }

    fun getWalletSession(): SessionData? {
        val walletAddress = sharedPreferences.getString("wallet_address", null)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val deviceId = sharedPreferences.getString("device_id", null)
        val timestamp = sharedPreferences.getString("session_timestamp", null)?.toLongOrNull()

        if (walletAddress != null && sessionToken != null && deviceId != null && timestamp != null) {
            if (System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000) {
                clearWalletSession()
                return null
            }
            return SessionData(walletAddress, sessionToken, deviceId, timestamp)
        }
        return null
    }

    private fun clearWalletSession() {
        sharedPreferences.edit().clear().apply()
    }

    private fun encrypt(value: String?): String? {
        if (value == null) return null
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = generateKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedValue = cipher.doFinal(value.toByteArray())
        return Base64.encodeToString(iv + encryptedValue, Base64.DEFAULT)
    }

    private fun decrypt(value: String?): String? {
        if (value == null) return null
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = generateKey()
        val decodedValue = Base64.decode(value, Base64.DEFAULT)
        val iv = decodedValue.copyOfRange(0, 16)
        val encryptedValue = decodedValue.copyOfRange(16, decodedValue.size)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(encryptedValue))
    }

    private fun generateKey(): SecretKey {
        val keyBytes = masterKeyAlias.toByteArray()
        return SecretKeySpec(keyBytes, "AES")
    }
}


data class SessionData(
    val walletAddress: String,
    val sessionToken: String,
    val deviceId: String,
    val timestamp: Long
)
