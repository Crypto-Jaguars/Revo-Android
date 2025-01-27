package com.example.fideicomisoapproverring.wallet

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class WalletRepository(private val context: Context) {
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "wallet_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveWalletConnection(publicKey: String) {
        securePreferences.edit()
            .putString("public_key", publicKey)
            .putBoolean("is_connected", true)
            .apply()
    }

    fun getStoredWallet(): String? {
        return securePreferences.getString("public_key", null)
    }

    fun clearWalletConnection() {
        securePreferences.edit().clear().apply()
    }
}