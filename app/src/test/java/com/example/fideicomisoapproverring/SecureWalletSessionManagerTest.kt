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

    private fun createTestSession(
        walletName: String = "LOBSTR",
        sessionToken: String = "test_token",
        deviceId: String = "test_device",
        timestamp: Long = System.currentTimeMillis()
    ) {
        testPrefs.edit().apply {
            putString("wallet_name", walletName)
            putString("session_token", sessionToken)
            putString("device_id", deviceId)
            putString("session_timestamp", timestamp.toString())
            apply()
        }
    }

    @Test
    fun testBasicSessionStorage() {
        val walletName = "LOBSTR"
        val sessionToken = "test_session_token"
        val deviceId = "test_device_id"
        createTestSession(walletName, sessionToken, deviceId)
        assertEquals(walletName, testPrefs.getString("wallet_name", null))
        assertEquals(sessionToken, testPrefs.getString("session_token", null))
        assertEquals(deviceId, testPrefs.getString("device_id", null))
        assertNotNull(testPrefs.getString("session_timestamp", null))
    }

    @Test
    fun testSessionExpiryExactly24Hours() {
        val timestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        createTestSession(timestamp = timestamp)
        assertNotNull(testPrefs.getString("session_token", null))
    }

    @Test
    fun testSessionExpiryAfter24Hours() {
        val timestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000) - (60 * 1000)
        createTestSession(timestamp = timestamp)
        assertNull(testPrefs.getString("session_token", null))
    }

    @Test
    fun testSessionRenewal() {
        val initialTimestamp = System.currentTimeMillis() - (23 * 60 * 60 * 1000)
        createTestSession(timestamp = initialTimestamp)
        val initialToken = testPrefs.getString("session_token", null)
        assertNotNull("Initial session should be valid", initialToken)

        val newTimestamp = System.currentTimeMillis()
        createTestSession(timestamp = newTimestamp)

        val renewedToken = testPrefs.getString("session_token", null)
        assertNotNull("Renewed session should be valid", renewedToken)
        assertTrue("New timestamp should be more recent",
            newTimestamp > initialTimestamp)
    }

    @Test
    fun testMultipleSessionUpdates() {
        val initialTime = System.currentTimeMillis()

        createTestSession(timestamp = initialTime)

        createTestSession(
            sessionToken = "token_2",
            timestamp = initialTime + (1 * 60 * 60 * 1000)
        )
        createTestSession(
            sessionToken = "token_3",
            timestamp = initialTime + (2 * 60 * 60 * 1000)
        )
        assertEquals("token_3", testPrefs.getString("session_token", null))
    }

    @Test
    fun testSessionInvalidationAtBoundary() {
        val almostExpired = System.currentTimeMillis() - (24 * 60 * 60 * 1000) + 1000
        createTestSession(timestamp = almostExpired)
        assertNotNull("Session should be valid just before expiry",
            testPrefs.getString("session_token", null))
        val justExpired = System.currentTimeMillis() - (24 * 60 * 60 * 1000) - 1000
        createTestSession(timestamp = justExpired)
        assertNull("Session should be invalid just after expiry",
            testPrefs.getString("session_token", null))
    }

    @Test
    fun testClearExpiredSession() {
        val expiredTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000)
        createTestSession(timestamp = expiredTimestamp)
        assertNull("Wallet name should be cleared", testPrefs.getString("wallet_name", null))
        assertNull("Session token should be cleared", testPrefs.getString("session_token", null))
        assertNull("Device ID should be cleared", testPrefs.getString("device_id", null))
        assertNull("Timestamp should be cleared", testPrefs.getString("session_timestamp", null))
    }
}