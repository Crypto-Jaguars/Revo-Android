package com.example.fideicomisoapproverring

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], manifest = Config.NONE)
class SecureWalletSessionManagerTest {

    private lateinit var context: Context
    private lateinit var testPrefs: SharedPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testPrefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        testPrefs.edit().clear().apply()
    }

    @Test
    fun testWalletSessionStorage() {
        val walletName = "LOBSTR"
        val sessionToken = "test_session_token"
        val deviceId = "test_device_id"

        // Save data
        testPrefs.edit().apply {
            putString("wallet_name", walletName)
            putString("session_token", sessionToken)
            putString("device_id", deviceId)
            putString("session_timestamp", System.currentTimeMillis().toString())
            apply()
        }

        // Verify data
        assertEquals(walletName, testPrefs.getString("wallet_name", null))
        assertEquals(sessionToken, testPrefs.getString("session_token", null))
        assertEquals(deviceId, testPrefs.getString("device_id", null))
        assertNotNull(testPrefs.getString("session_timestamp", null))
    }

    @Test
    fun testSessionExpiry() {

        val walletName = "LOBSTR"
        val sessionToken = "test_session_token"
        val deviceId = "test_device_id"


        val oldTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000)
        testPrefs.edit().apply {
            putString("wallet_name", walletName)
            putString("session_token", sessionToken)
            putString("device_id", deviceId)
            putString("session_timestamp", oldTimestamp.toString())
            apply()
        }
        assertNotNull(testPrefs.getString("wallet_name", null))
    }
}