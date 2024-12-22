package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import org.stellar.sdk.*
import org.stellar.sdk.responses.SubmitTransactionResponse
import java.lang.Exception

class FindEscrowActivity : AppCompatActivity() {

    private lateinit var loadingPanel: LinearLayout
    private lateinit var form: LinearLayout

    private lateinit var engagementIdInput: EditText
    private lateinit var contractIdInput: EditText
    private lateinit var enterButton: Button
    private lateinit var balanceTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_escrow)

        // Initialize components
        loadingPanel = findViewById(R.id.loadingPanel)
        form = findViewById(R.id.form)
        engagementIdInput = findViewById(R.id.engagementIdInput)
        contractIdInput = findViewById(R.id.contractIdInput)
        enterButton = findViewById(R.id.enterButton)
        balanceTextView = findViewById(R.id.balanceTextView)
        logoutButton = findViewById(R.id.logoutButton)

        // Manages the logic of the “Enter” button.
        enterButton.setOnClickListener {
            val engagementId = engagementIdInput.text.toString()
            val contractId = contractIdInput.text.toString()

            if (engagementId.isNotEmpty() && contractId.isNotEmpty()) {
                loadingPanel.visibility = View.VISIBLE
                fetchEngagementData(engagementId, contractId)
                hideKeyboard()
            } else {
                Toast.makeText(this, "Please enter an Engagement ID and a Contract ID.", Toast.LENGTH_SHORT).show()
            }
        }

        setupSignTransactionButton()

        // Manages the “Check Balance” button
        val checkBalanceButton: Button = findViewById(R.id.checkBalanceButton)
        checkBalanceButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
            val publicKey = sharedPreferences.getString("publicKey", null)

            if (!publicKey.isNullOrEmpty()) {
                fetchBalance(publicKey)
            } else {
                Toast.makeText(this, "Public key not found. Please log in again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Manages the “Logout” button
        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(enterButton.windowToken, 0)
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("isWalletConnected")
            .remove("publicKey")
            .apply()

        Toast.makeText(this, "Wallet disconnected.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, EscrowApproverActivity::class.java)
        startActivity(intent)
        finish()
    }

    protected override fun onResume() {
        super.onResume()
        loadingPanel.visibility = View.GONE
        engagementIdInput.setText("")
        contractIdInput.setText("")
    }

    private fun fetchEngagementData(engagementId: String, contractId: String) {
        val url = "https://api.trustlesswork.com/escrow/get-escrow-by-engagement-id?contractId=$contractId&engagementId=$engagementId"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loadingPanel.visibility = View.GONE
                    Toast.makeText(this@FindEscrowActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        val intent = Intent(this@FindEscrowActivity, EscrowDetailsActivity::class.java)
                        if (response.code == 200) {
                            intent.putExtra("escrowData", responseBody.toString())
                            intent.putExtra("engagementID", engagementId)
                        }
                        startActivity(intent)
                    }
                } else {
                    runOnUiThread {
                        val intent = Intent(this@FindEscrowActivity, EscrowDetailsActivity::class.java)
                        intent.putExtra("engagementID", engagementId)
                        startActivity(intent)
                    }
                }
            }
        })
    }

    private fun fetchBalance(publicKey: String) {
        val url = "https://horizon-testnet.stellar.org/accounts/$publicKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@FindEscrowActivity, "Error connecting to server: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val balance = parseBalance(responseBody)
                    runOnUiThread {
                        balanceTextView.visibility = View.VISIBLE
                        balanceTextView.text = "Balance: $balance XLM"
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@FindEscrowActivity, "Account not found or invalid.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun parseBalance(response: String?): String {
        return try {
            val json = JSONObject(response ?: return "0")
            val balances = json.getJSONArray("balances")
            for (i in 0 until balances.length()) {
                val balance = balances.getJSONObject(i)
                if (balance.getString("asset_type") == "native") {
                    return balance.getString("balance")
                }
            }
            "0"
        } catch (e: Exception) {
            "Error when analyzing balance"
        }
    }

    private fun setupSignTransactionButton() {
        val signTransactionButton: Button = findViewById(R.id.signTransactionButton)
        signTransactionButton.setOnClickListener {
            // Recuperar clave pública desde SharedPreferences
            val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
            val publicKey = sharedPreferences.getString("publicKey", null)

            if (publicKey.isNullOrEmpty()) {
                Toast.makeText(this, "Public key not found. Please connect your wallet.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar diálogo para ingresar la clave privada
            val privateKeyInput = EditText(this).apply {
                hint = "Enter Private Key"
            }

            AlertDialog.Builder(this)
                .setTitle("Sign Transaction")
                .setMessage("Enter your private key to sign the transaction.")
                .setView(privateKeyInput)
                .setPositiveButton("Sign") { _, _ ->
                    val privateKey = privateKeyInput.text.toString()
                    if (privateKey.isNotEmpty()) {
                        // Llamar a la función para firmar y enviar la transacción
                        signAndSendTransaction(privateKey, publicKey)
                    } else {
                        Toast.makeText(this, "Private key is required to sign.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun signAndSendTransaction(privateKey: String, publicKey: String) {
        try {
            // Conectar al servidor de Horizon (Testnet)
            val server = Server("https://horizon-testnet.stellar.org")

            // Obtener el estado de la cuenta
            val sourceAccount = server.accounts().account(publicKey)

            // Crear la transacción
            val transaction = Transaction.Builder(sourceAccount, Network.TESTNET)
                .addOperation(
                    PaymentOperation.Builder(
                        "GCDXSOPD5T5MCGCPPRV3CWMYLTTWASVZBF4HZBNES6PGK7YRATM27NB4", // Cambia esto por la clave pública de destino
                        AssetTypeNative(),
                        "1000" // Cantidad a enviar (en XLM)
                    ).build()
                )
                .setTimeout((System.currentTimeMillis() / 1000) + 300) // Configura un timeout de 5 minutos desde ahora
                .setBaseFee(Transaction.MIN_BASE_FEE.toLong())
                .build()

            // Firmar la transacción con la clave privada proporcionada
            val keyPair = KeyPair.fromSecretSeed(privateKey)
            transaction.sign(keyPair)

            // Enviar la transacción a la red Stellar
            val response: SubmitTransactionResponse = server.submitTransaction(transaction)

            // Mostrar el resultado
            if (response.isSuccess) {
                runOnUiThread {
                    Toast.makeText(this, "Transaction successful!", Toast.LENGTH_LONG).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Transaction failed: ${response.resultXdr}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error signing transaction: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
