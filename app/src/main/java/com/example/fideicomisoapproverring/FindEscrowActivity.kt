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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindEscrowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        transactionMonitor = TransactionMonitor()

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

        val statusBanner: RelativeLayout = binding.statusBanner
        val statusIcon: ImageView = binding.statusIcon
        val statusText: TextView = binding.statusText

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
            val publicKey = sharedPreferences.getString("publicKey", null)
                val rawPrivateKey = BuildConfig.STELLAR_PRIVATE_KEY

                Log.d("TransactionFlow", "Private key validation:")
                Log.d("TransactionFlow", "Raw value from BuildConfig: '${rawPrivateKey}'")
                Log.d("TransactionFlow", "Length: ${rawPrivateKey.length}")
                Log.d("TransactionFlow", "Contains quotes: ${rawPrivateKey.contains("\"")}")
                Log.d("TransactionFlow", "Trimmed length: ${rawPrivateKey.trim().length}")
                Log.d("TransactionFlow", "First char: ${rawPrivateKey.firstOrNull()}")
                Log.d("TransactionFlow", "Last char: ${rawPrivateKey.lastOrNull()}")
                Log.d("TransactionFlow", "All chars: ${rawPrivateKey.toCharArray().joinToString(",")}")

                // Clean up the private key
                val privateKey = rawPrivateKey.trim().replace("\"", "")

                if (publicKey.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(this@FindEscrowActivity, "Please connect your wallet first", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                if (privateKey.isNullOrEmpty() || !isValidStellarPrivateKey(privateKey)) {
                    Log.e("TransactionFlow", "Invalid private key configuration. Length: ${privateKey?.length ?: 0}")
                    Log.e("TransactionFlow", "Private key format check: starts with 'S': ${privateKey?.startsWith("S") ?: false}")
                    Log.e("TransactionFlow", "Private key format check: length is 56: ${privateKey?.length == 56}")
                    if (!privateKey.isNullOrEmpty()) {
                        val base32Regex = "^[A-Z2-7]+$".toRegex()
                        Log.e("TransactionFlow", "Private key format check: base32 regex match: ${base32Regex.matches(privateKey.substring(1))}")
                    }
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(this@FindEscrowActivity, "Invalid private key configuration", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                Log.d("TransactionFlow", "Validating public key format: $publicKey")
                if (!isValidStellarPublicKey(publicKey)) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(this@FindEscrowActivity, "Invalid public key format", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                Log.d("TransactionFlow", "Connecting wallet with public key: $publicKey")
                transactionMonitor.onWalletConnection(transactionId, true)
                
                // Set up network and server
            val server = Server("https://horizon-testnet.stellar.org")

                withContext(Dispatchers.Main) {
                    showLoading(true)
                }

                Log.d("TransactionFlow", "Fetching source account from Stellar network")
                val sourceAccount = try {
                    server.accounts().account(publicKey)
                } catch (e: Exception) {
                    Log.e("TransactionFlow", "Exception during account fetch: ${e.message}")
                    Log.e("TransactionFlow", e.stackTraceToString())
                    
                    // Check if the account doesn't exist
                    if (e is org.stellar.sdk.requests.ErrorResponse && e.message?.contains("not found") == true) {
                        withContext(Dispatchers.Main) {
                            showLoading(false)
                            Toast.makeText(this@FindEscrowActivity, "Account not found on Stellar testnet. Please fund your account first.", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                    throw e
                }

                Log.d("TransactionFlow", "Building transaction")
                transactionMonitor.onTransactionSigning(transactionId)

            val transaction = Transaction.Builder(sourceAccount, Network.TESTNET)
                .addOperation(
                    PaymentOperation.Builder(
                        "GACLZTQDEX4UQVCXVWJDE3VN353OWQEBABW4R5RJDPGKESGEHE5Z7CNV", // Using a known valid public key for testing
                        AssetTypeNative(),
                        "10"
                    ).build()
                )
                    .setTimeout(180)
                    .setBaseFee(100)
                .build()

                Log.d("TransactionFlow", "Signing transaction")
            val keyPair = KeyPair.fromSecretSeed(privateKey)
            transaction.sign(keyPair)
                transactionMonitor.onTransactionSigned(transactionId, true)

                Log.d("TransactionFlow", "Submitting transaction")
                try {
                    val response = server.submitTransaction(transaction)
                    Log.d("TransactionFlow", "Transaction response: ${response.hash}")
                    transactionMonitor.onTransactionSubmission(transactionId, response)
                    
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        if (response.isSuccess) {
                            Toast.makeText(this@FindEscrowActivity, "Transaction successful!", Toast.LENGTH_SHORT).show()
                            // Refresh balance after successful transaction
                            Handler(Looper.getMainLooper()).postDelayed({
                                fetchBalance(publicKey)
                            }, 2000)
                        } else {
                            handleTransactionError(transactionId, "TRANSACTION_ERROR", "Transaction failed: ${response.extras?.resultCodes?.transactionResultCode}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TransactionFlow", "Exception during transaction: ${e.message}")
                    Log.e("TransactionFlow", e.stackTraceToString())
                    
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        handleTransactionError(transactionId, "TRANSACTION_ERROR", e.message ?: "Unknown error")
                    }
                }
            } catch (e: Exception) {
                Log.e("TransactionFlow", "Exception during transaction: ${e.message}")
                Log.e("TransactionFlow", e.stackTraceToString())
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    handleTransactionError(transactionId, "WALLET_ERROR", e.message ?: "Unknown error")
                }
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
    }

    private fun handleTransactionError(transactionId: String, errorType: String, errorMessage: String) {
        val state = transactionMonitor.getTransactionState(transactionId)
        val error = state.error
        
        Log.e("TransactionFlow", """
            Error details:
            Transaction ID: $transactionId
            State: ${state.status}
            Error Type: $errorType
            Error Message: $errorMessage
            Recoverable: ${error?.recoverable}
        """.trimIndent())
        
            runOnUiThread {
            if (error != null) {
                val intent = Intent(this, ErrorPageActivity::class.java)
                intent.putExtra(ErrorPageActivity.ERROR_TYPE, errorType)
                intent.putExtra("error_title", getFriendlyErrorTitle(error.type))
                intent.putExtra("error_message", getFriendlyErrorMessage(error))
                intent.putExtra("error_recovery", getRecoverySteps(error))
                startActivity(intent)
            } else {
                Toast.makeText(this, "❌ Transaction could not be completed. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFriendlyErrorTitle(errorType: TransactionErrorHandler.Companion.ErrorType): String {
        return when (errorType) {
            TransactionErrorHandler.Companion.ErrorType.NETWORK -> "Connection Issue"
            TransactionErrorHandler.Companion.ErrorType.BLOCKCHAIN -> "Transaction Issue"
            TransactionErrorHandler.Companion.ErrorType.WALLET -> "Wallet Issue"
            TransactionErrorHandler.Companion.ErrorType.CONTRACT -> "Smart Contract Issue"
            TransactionErrorHandler.Companion.ErrorType.UNKNOWN -> "Unexpected Issue"
        }
    }

    private fun getFriendlyErrorMessage(error: TransactionErrorHandler.TransactionError): String {
        return when (error.type) {
            TransactionErrorHandler.Companion.ErrorType.NETWORK -> 
                "We couldn't connect to the network. This might be due to poor internet connection."
            TransactionErrorHandler.Companion.ErrorType.BLOCKCHAIN -> 
                when (error.code) {
                    "INSUFFICIENT_BALANCE" -> "There aren't enough funds to complete this transaction."
                    "INSUFFICIENT_FEE" -> "The network is busy. Please try again with a higher transaction fee."
                    else -> "There was an issue processing your transaction on the blockchain."
                }
            TransactionErrorHandler.Companion.ErrorType.WALLET -> 
                "There was an issue with your wallet connection. Please ensure your wallet is properly connected."
            TransactionErrorHandler.Companion.ErrorType.CONTRACT -> 
                "There was an issue with the smart contract. Your funds are safe and have not been moved."
            TransactionErrorHandler.Companion.ErrorType.UNKNOWN -> 
                "An unexpected issue occurred. Don't worry, your funds are safe."
        }
    }

    private fun getRecoverySteps(error: TransactionErrorHandler.TransactionError): String {
        val baseSteps = when (error.type) {
            TransactionErrorHandler.Companion.ErrorType.NETWORK -> 
                "1. Check your internet connection\n2. Wait a few moments\n3. Try the transaction again"
            TransactionErrorHandler.Companion.ErrorType.BLOCKCHAIN -> 
                when (error.code) {
                    "INSUFFICIENT_BALANCE" -> "1. Check your wallet balance\n2. Reduce the transaction amount\n3. Try again"
                    "INSUFFICIENT_FEE" -> "1. Wait a few minutes for network congestion to reduce\n2. Try the transaction again"
                    else -> "1. Wait a few moments\n2. Try the transaction again\n3. If the issue persists, contact support"
                }
            TransactionErrorHandler.Companion.ErrorType.WALLET -> 
                "1. Check your wallet connection\n2. Refresh the page\n3. Try connecting your wallet again"
            TransactionErrorHandler.Companion.ErrorType.CONTRACT -> 
                "1. Wait a few moments\n2. Try the transaction again\n3. If the issue persists, contact support"
            TransactionErrorHandler.Companion.ErrorType.UNKNOWN -> 
                "1. Wait a few moments\n2. Try the transaction again\n3. If the issue persists, contact support"
        }
        return baseSteps + "\n\nTransaction ID: ${error.transactionId}"
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
