package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import org.stellar.sdk.*
import org.stellar.sdk.responses.SubmitTransactionResponse
import java.lang.Exception
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import com.example.fideicomisoapproverring.recovery.transaction.AtomicTransactionManager
import com.example.fideicomisoapproverring.recovery.ui.TransactionRecoveryDialog
import com.example.fideicomisoapproverring.recovery.model.TransactionError

@AndroidEntryPoint
class FindEscrowActivity : AppCompatActivity() {

    private lateinit var loadingPanel: LinearLayout
    private lateinit var form: LinearLayout

    private lateinit var engagementIdInput: EditText
    private lateinit var contractIdInput: EditText
    private lateinit var enterButton: Button
    private lateinit var balanceTextView: TextView
    private lateinit var logoutButton: Button

    private lateinit var statusBanner: View
    private lateinit var statusIcon: ImageView
    private lateinit var statusText: TextView

    @Inject
    lateinit var transactionManager: AtomicTransactionManager

    @Inject
    lateinit var recoveryService: TransactionRecoveryService

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

        statusBanner = findViewById(R.id.statusBanner)
        statusIcon = findViewById(R.id.statusIcon)
        statusText = findViewById(R.id.statusText)

        val statusBanner: RelativeLayout = findViewById(R.id.statusBanner)
        val statusIcon: ImageView = findViewById(R.id.statusIcon)
        val statusText: TextView = findViewById(R.id.statusText)

        // Obtener el estado de la conexión desde el intent
        val connectionStatus = intent.getStringExtra("connectionStatus")

        if (connectionStatus == ConnectionStatus.SUCCESS.name) {
            showStatusBanner(ConnectionStatus.SUCCESS, statusBanner, statusIcon, statusText)
        }

        // Manages the logic of the "Enter" button.
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

        // Manages the "Check Balance" button
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

        // Manages the "Logout" button
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

            val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
            val publicKey = sharedPreferences.getString("publicKey", null)

            if (publicKey.isNullOrEmpty()) {
                Toast.makeText(this, "Public key not found. Please connect your wallet.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


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
        lifecycleScope.launch {
            try {
                val result = transactionManager.executeTransaction(
                    sourceAccount = publicKey,
                    destinationAccount = "GCDXSOPD5T5MCGCPPRV3CWMYLTTWASVZBF4HZBNES6PGK7YRATM27NB4",
                    amount = "1000",
                    memo = "Escrow Payment"
                )
                
                when (result) {
                    is AtomicTransactionManager.TransactionState.Success -> {
                        runOnUiThread {
                            Toast.makeText(this@FindEscrowActivity, 
                                "Transaction successful!", 
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is AtomicTransactionManager.TransactionState.Error -> {
                        handleTransactionError(result.error)
                    }
                    is AtomicTransactionManager.TransactionState.Pending -> {
                        runOnUiThread {
                            Toast.makeText(this@FindEscrowActivity,
                                "Transaction is pending...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@FindEscrowActivity,
                        "Error signing transaction: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleTransactionError(error: TransactionError) {
        runOnUiThread {
            TransactionRecoveryDialog(
                context = this,
                transactionError = error,
                recoveryService = recoveryService
            ).show()
        }
    }

    private fun showStatusBanner(status: ConnectionStatus, banner: RelativeLayout, icon: ImageView, text: TextView) {
        when (status) {
            ConnectionStatus.SUCCESS -> {
                banner.setBackgroundResource(R.drawable.toast_success)
                icon.setImageResource(R.drawable.check)
                text.text = "Connection Successfully"
            }
            ConnectionStatus.ERROR -> {
                banner.setBackgroundResource(R.drawable.toast_error)
                icon.setImageResource(R.drawable.cancel)
                text.text = "Error: Unable to connect."
            }
            ConnectionStatus.WARNING -> {
                banner.setBackgroundResource(R.drawable.toast_warning)
                icon.setImageResource(R.drawable.info)
                text.text = "Action Required: Please check your input."
            }
        }

        banner.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            banner.visibility = View.GONE
        }, 3000)
    }

}
