package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.example.fideicomisoapproverring.transaction.TransactionMonitor
import com.example.fideicomisoapproverring.transaction.TransactionErrorHandler
import com.example.fideicomisoapproverring.transaction.TransactionRetryManager
import com.example.fideicomisoapproverring.transaction.TransactionStateManager
import com.example.fideicomisoapproverring.transaction.TransactionStatusView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import org.stellar.sdk.*
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.SubmitTransactionResponse
import java.lang.Exception
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.fideicomisoapproverring.databinding.ActivityFindEscrowBinding
import java.util.Random
import org.stellar.sdk.Network
import kotlinx.coroutines.delay
import org.stellar.sdk.Server

class FindEscrowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindEscrowBinding
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

    private lateinit var transactionMonitor: TransactionMonitor
    private lateinit var transactionRetryManager: TransactionRetryManager
    private lateinit var transactionStateManager: TransactionStateManager

    private lateinit var transactionStatusView: TransactionStatusView

    private lateinit var server: Server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindEscrowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize server
        server = Server("https://horizon-testnet.stellar.org")
        
        transactionMonitor = TransactionMonitor()
        transactionRetryManager = TransactionRetryManager()
        transactionStateManager = TransactionStateManager(this)

        // Load any persisted transaction states
        lifecycleScope.launch {
            transactionStateManager.loadPersistedStates()
            // Clean up old transactions
            transactionStateManager.cleanupOldTransactions()
        }

        // Initialize components
        loadingPanel = binding.loadingPanel
        form = binding.form
        engagementIdInput = binding.engagementIdInput
        contractIdInput = binding.contractIdInput
        enterButton = binding.enterButton
        balanceTextView = binding.balanceTextView
        logoutButton = binding.logoutButton

        statusBanner = binding.statusBanner
        statusIcon = binding.statusIcon
        statusText = binding.statusText

        transactionStatusView = binding.transactionStatusView

        setupUI()

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
        val checkBalanceButton: Button = binding.checkBalanceButton
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
        binding.signTransactionButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                signAndSendTransaction()
            }
        }
    }

    private fun signAndSendTransaction() {
        val transactionId = "TX_${System.currentTimeMillis()}_${Random().nextInt(10000)}"
        Log.d("TransactionFlow", "Starting transaction $transactionId")
        transactionMonitor.startTransaction(transactionId)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
                val publicKey = sharedPreferences.getString("publicKey", null)?.trim()
                val rawPrivateKey = BuildConfig.STELLAR_PRIVATE_KEY.trim()

                // Validate keys before proceeding
                if (publicKey.isNullOrEmpty() || !isValidStellarPublicKey(publicKey)) {
                    throw IllegalArgumentException("Invalid public key format")
                }

                if (rawPrivateKey.isNullOrEmpty() || !isValidStellarPrivateKey(rawPrivateKey)) {
                    throw IllegalArgumentException("Invalid private key format")
                }

                // Clean destination account
                val destinationAccount = "GACLZTQDEX4UQVCXVWJDE3VN353OWQEBABW4R5RJDPGKESGEHE5Z7CNV".trim()
                if (!isValidStellarPublicKey(destinationAccount)) {
                    throw IllegalArgumentException("Invalid destination account format")
                }

                val amount = "10"

                // Save initial transaction state
                transactionStateManager.saveTransactionState(
                    transactionId = transactionId,
                    sourceAccount = publicKey,
                    destinationAccount = destinationAccount,
                    amount = amount
                )

                withContext(Dispatchers.Main) {
                    showLoading(true)
                }

                val retryAction = suspend {
                    try {
                        Log.d("TransactionFlow", "Fetching source account from Stellar network")
                        val sourceAccount = server.accounts().account(publicKey)

                        Log.d("TransactionFlow", "Building transaction")
                        transactionMonitor.onTransactionSigning(transactionId)

                        val transaction = Transaction.Builder(sourceAccount, Network.TESTNET)
                            .addOperation(
                                PaymentOperation.Builder(
                                    destinationAccount,
                                    AssetTypeNative(),
                                    amount
                                ).build()
                            )
                            .setTimeout(180)
                            .setBaseFee(100)
                            .build()

                        Log.d("TransactionFlow", "Signing transaction")
                        val keyPair = KeyPair.fromSecretSeed(rawPrivateKey)
                        transaction.sign(keyPair)
                        transactionMonitor.onTransactionSigned(transactionId, true)

                        Log.d("TransactionFlow", "Submitting transaction")
                        val response = server.submitTransaction(transaction)
                        Log.d("TransactionFlow", "Transaction response: ${response.hash}")
                        transactionMonitor.onTransactionSubmission(transactionId, response)

                        withContext(Dispatchers.Main) {
                            showLoading(false)
                            if (response.isSuccess) {
                                transactionRetryManager.resetRetryState(transactionId)
                                transactionStateManager.updateTransactionState(
                                    transactionId = transactionId,
                                    status = TransactionStateManager.TransactionStatus.COMPLETED,
                                    response = response
                                )
                                Toast.makeText(this@FindEscrowActivity, "Transaction successful!", Toast.LENGTH_SHORT).show()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    fetchBalance(publicKey)
                                }, 2000)
                    } else {
                                transactionStateManager.updateTransactionState(
                                    transactionId = transactionId,
                                    status = TransactionStateManager.TransactionStatus.FAILED
                                )
                                val error = TransactionErrorHandler().handleStellarError(response, transactionId)
                                throw Exception(error.message)
                            }
                        }
                    } catch (e: Exception) {
                        throw e
                    }
                    Unit
                }

                try {
                    retryAction()
                } catch (e: Exception) {
                    Log.e("TransactionFlow", "Final exception during transaction: ${e.message}")
                    Log.e("TransactionFlow", e.stackTraceToString())
                    
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        handleTransactionError(transactionId, "WALLET_ERROR", e.message ?: "Unknown error")
                    }
                }
            } catch (e: Exception) {
                Log.e("TransactionFlow", "Error in transaction setup: ${e.message}")
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    handleTransactionError(transactionId, "SETUP_ERROR", e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Performs a rollback operation for a failed transaction
     */
    private suspend fun performRollback(transactionId: String) {
        try {
            transactionStatusView.showRollbackInProgress()
            
            val state = transactionStateManager.getTransactionState(transactionId) ?: return
            val server = Server("https://horizon-testnet.stellar.org")

            // Create a reverse transaction
            val sourceAccount = server.accounts().account(state.destinationAccount)
            val transaction = Transaction.Builder(sourceAccount, Network.TESTNET)
                .addOperation(
                    PaymentOperation.Builder(
                        state.sourceAccount,
                        AssetTypeNative(),
                        state.amount
                    ).build()
                )
                .setTimeout(180)
                .setBaseFee(100)
                .build()

            // Submit rollback transaction
            val response = server.submitTransaction(transaction)
            if (response.isSuccess) {
                transactionStateManager.completeRollback(transactionId, response)
                Log.d("TransactionFlow", "Rollback successful for transaction $transactionId")
                withContext(Dispatchers.Main) {
                    transactionStatusView.showRollbackComplete()
                }
            } else {
                Log.e("TransactionFlow", "Rollback failed for transaction $transactionId")
                withContext(Dispatchers.Main) {
                    handleTransactionError(transactionId, "ROLLBACK_ERROR", "Failed to rollback transaction")
                }
            }
        } catch (e: Exception) {
            Log.e("TransactionFlow", "Error during rollback: ${e.message}")
            withContext(Dispatchers.Main) {
                handleTransactionError(transactionId, "ROLLBACK_ERROR", e.message ?: "Unknown error during rollback")
            }
        }
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
            Log.e("TransactionFlow", "Error validating public key: ${e.message}")
            false
        }
    }

    private fun isValidStellarPrivateKey(privateKey: String): Boolean {
        return try {
            if (!privateKey.startsWith("S")) {
                return false
            }
            if (privateKey.length != 56) {
                return false
            }
            val base32Regex = "^[A-Z2-7]+$".toRegex()
            if (!base32Regex.matches(privateKey.substring(1))) {
                return false
            }
            // Try to create a KeyPair from the private key to validate it
            KeyPair.fromSecretSeed(privateKey)
            true
        } catch (e: Exception) {
            Log.e("TransactionFlow", "Error validating private key: ${e.message}")
            false
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingPanel.visibility = if (show) View.VISIBLE else View.GONE
        binding.form.visibility = if (show) View.GONE else View.VISIBLE
        if (show) {
            binding.transactionStatusView.visibility = View.VISIBLE
            binding.transactionStatusView.showTransactionInProgress()
        } else {
            binding.transactionStatusView.visibility = View.GONE
        }
    }

    private fun handleTransactionError(transactionId: String, errorCode: String, errorMessage: String) {
        val error = TransactionErrorHandler().handleWalletError(
            Exception(errorMessage),
            transactionId
        )
        
        // Show error in TransactionStatusView
        transactionStatusView.showTransactionError(
            error = error,
            friendlyMessage = error.userMessage,
            steps = "1. Check your connection\n2. Verify your wallet details\n3. Try again"
        )
        
        // Show a toast with the user-friendly message
        Toast.makeText(this, error.userMessage, Toast.LENGTH_LONG).show()
        
        // Log the technical details
        Log.e("TransactionFlow", """
            Error details:
            Transaction ID: $transactionId
            State: ${transactionStateManager.getTransactionState(transactionId)?.status}
            Error Type: ${error.type}
            Error Message: ${error.message}
            Recoverable: ${error.recoverable}
        """.trimIndent())
    }

    private fun setupUI() {
        // Get connection status from intent
        val connectionStatus = intent.getStringExtra("connection_status")

        if (connectionStatus == ConnectionStatus.SUCCESS.name) {
            binding.statusBanner.apply {
                visibility = View.VISIBLE
            }
            binding.statusIcon.setImageResource(R.drawable.check)
            binding.statusText.text = "Connection Successful"
        }

        setupButtons()
    }

    private fun setupButtons() {
        binding.enterButton.setOnClickListener {
            val engagementId = binding.engagementIdInput.text.toString()
            val contractId = binding.contractIdInput.text.toString()
            
            if (engagementId.isBlank() || contractId.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    showLoading(true)
                    // Fetch engagement data would go here
                    delay(1000) // Simulating API call
                    showLoading(false)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(this@FindEscrowActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.logoutButton.setOnClickListener {
            // Clear wallet connection
            val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            
            // Return to main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.checkBalanceButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
                    val publicKey = sharedPreferences.getString("publicKey", null)
                    
                    if (publicKey == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FindEscrowActivity, "Please connect your wallet first", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val balance = checkBalance(publicKey)
                    withContext(Dispatchers.Main) {
                        binding.balanceTextView.text = "Balance: $balance XLM"
                        binding.balanceTextView.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FindEscrowActivity, "Error checking balance: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        setupSignTransactionButton()
    }

    private suspend fun checkBalance(publicKey: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://horizon-testnet.stellar.org/accounts/$publicKey")
                .build()

            try {
                val response = client.newCall(request).execute()
                val json = JSONObject(response.body?.string() ?: throw Exception("Empty response"))
                
                val balances = json.getJSONArray("balances")
                for (i in 0 until balances.length()) {
                    val balance = balances.getJSONObject(i)
                    if (balance.getString("asset_type") == "native") {
                        return@withContext balance.getString("balance")
                    }
                }
                "0"
            } catch (e: Exception) {
                Log.e("TransactionFlow", "Error checking balance: ${e.message}")
                throw e
            }
        }
    }

}
