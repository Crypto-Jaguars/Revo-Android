// MainActivity.kt
package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fideicomisoapproverring.security.SecureWalletSessionManager
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var engadmentIdInput: EditText
    private lateinit var enterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val secureSessionManager = SecureWalletSessionManager(applicationContext)


        val session = secureSessionManager.getWalletSession()
        session?.let {
            Log.d("WalletSession", "Wallet: ${it.walletAddress}, Session: ${it.sessionToken}")
        } ?: run {
            Log.d("WalletSession", "No active session found.")
        }

        engadmentIdInput = findViewById(R.id.engagementIdInput)
        enterButton = findViewById(R.id.enterButton)

        enterButton.setOnClickListener {
            val engadmentId = engadmentIdInput.text.toString()

            if (engadmentId.isNotEmpty()) {
                fetchEngagementData(engadmentId)
            } else {
                Toast.makeText(this, "Por favor ingresa un Engadment ID", Toast.LENGTH_SHORT).show()
            }
        }
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