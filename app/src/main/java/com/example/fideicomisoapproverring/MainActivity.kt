// MainActivity.kt
package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fideicomisoapproverring.security.SecureWalletSessionManager
import com.example.fideicomisoapproverring.security.SessionData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fideicomisoapproverring.guests.navigation.Routes
import com.example.fideicomisoapproverring.guests.ui.views.DashboardView
import com.example.fideicomisoapproverring.theme.ui.ThemeSettingsScreen
import com.example.fideicomisoapproverring.theme.ui.theme.RingCoreTheme
import com.example.fideicomisoapproverring.theme.ui.theme.rememberThemeManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SecureWalletSessionManager
    private val TAG = "SessionCheck"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.trustlesswork/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SecureWalletSessionManager(this)
        checkWalletSession()

        setContent {
            val navController = rememberNavController()
            val themeManager = rememberThemeManager(this)

            RingCoreTheme(
                themeManager = themeManager
            ) {
                NavHost(navController = navController, startDestination = Routes.Home.value) {
                    composable(route = Routes.Home.value) {
                        DashboardView(
                            navController = navController,
                            onThemeSettingsClick = {
                                navController.navigate(Routes.ThemeSettings.value)
                            }
                        )
                    }

                    composable(route = Routes.ThemeSettings.value) {
                        ThemeSettingsScreen(
                            themeManager = themeManager,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(route = Routes.Wallet.value) {
                    }

                    composable(route = Routes.Activity.value) {
                    }

                    composable(route = Routes.Search.value) {
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

    private fun fetchEngagementData(engagementId: String) {
        val contractId = ""
        val service = retrofit.create(EscrowService::class.java)
        val call = service.getEscrowByEngagementId(engagementId, contractId)

        call.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
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

interface EscrowService {
    @retrofit2.http.GET("escrow/get-escrow-by-engagement-id")
    fun getEscrowByEngagementId(
        @retrofit2.http.Query("engagementId") engagementId: String,
        @retrofit2.http.Query("contractId") contractId: String
    ): Call<String>
}
