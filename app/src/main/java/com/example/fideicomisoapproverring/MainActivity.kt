// MainActivity.kt
package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fideicomisoapproverring.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.fideicomisoapproverring.security.SecureWalletSessionManager
import com.example.fideicomisoapproverring.security.SessionData
import okhttp3.*
import java.io.IOException
import com.example.fideicomisoapproverring.guests.navigation.Routes
import com.example.fideicomisoapproverring.guests.ui.views.DashboardView
import com.example.fideicomisoapproverring.ui.screens.HomeScreen
import com.example.fideicomisoapproverring.ui.screens.EngagementScreen
import com.example.fideicomisoapproverring.ui.screens.EscrowScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var engadmentIdInput: EditText
    private lateinit var enterButton: Button
    private lateinit var sessionManager: SecureWalletSessionManager
    private val TAG = "SessionCheck"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SecureWalletSessionManager(this)
        checkWalletSession()

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("engagement") {
                            EngagementScreen(navController)
                        }
                        composable("escrow") {
                            EscrowScreen(navController)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkWalletSession()
    }

    private fun checkWalletSession() {
        val session = sessionManager.getWalletSession()
        if (session != null) {
            Log.d(TAG, "Active session found for wallet")

            handleActiveSession(session)
        } else {
            Log.d(TAG, "No active session found")
            showWalletSelection()
        }
    }

    private fun handleActiveSession(session: SessionData) {
        Log.d(TAG, "Processing session for ${session.walletName}")
    }

    private fun showWalletSelection() {
        val walletSelection = WalletSelection { selectedWallet ->
            Log.d(TAG, "New wallet selected: $selectedWallet")
        }
        walletSelection.show(supportFragmentManager, "WalletSelection")
    }

    private fun fetchEngagementData(engadmentId: String) {
        val contractId = ""
        val url = "https://api.trustlesswork/escrow/get-escrow-by-engagement-id?engagementId=$engadmentId&contractId=$contractId/"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error en la conexiÃ³n", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        val intent = Intent(this@MainActivity, EngagementActivity::class.java)
                        intent.putExtra("engagementData", responseBody)
                        startActivity(intent)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "ID no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
