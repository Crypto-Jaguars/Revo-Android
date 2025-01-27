package com.example.fideicomisoapproverring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import com.example.fideicomisoapproverring.wallet.WalletManager
import com.example.fideicomisoapproverring.wallet.WalletRepository
import com.example.fideicomisoapproverring.wallet.WalletState

class EscrowApproverActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var validateKeyButton: Button
    private lateinit var publicKeyInput: TextInputEditText
    private lateinit var walletManager: WalletManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escrow_approver)

        // Initialize UI elements
        connectButton = findViewById(R.id.connectButton)
        validateKeyButton = findViewById(R.id.validatePublicKeyButton)
        publicKeyInput = findViewById(R.id.publicKeyInput)

        // Initialize WalletManager
        walletManager = WalletManager(WalletRepository(applicationContext))

        // Observe wallet state
        lifecycleScope.launch {
            walletManager.walletState.collect { state ->
                when (state) {
                    is WalletState.Disconnected -> {
                        connectButton.isEnabled = true
                        validateKeyButton.isEnabled = false
                    }
                    is WalletState.Connecting -> {
                        connectButton.isEnabled = false
                        // Show loading indicator
                    }
                    is WalletState.Connected -> {
                        connectButton.isEnabled = false
                        validateKeyButton.isEnabled = true
                        publicKeyInput.setText(state.publicKey)
                    }
                    is WalletState.Error -> {
                        connectButton.isEnabled = true
                        Toast.makeText(this@EscrowApproverActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        publicKeyInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateKeyButton.isEnabled = !s.isNullOrEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        connectButton.setOnClickListener {
            showWalletSelectionDialog()
        }

        validateKeyButton.setOnClickListener {
            val publicKey = publicKeyInput.text.toString()
            if (publicKey.isNotEmpty()) {
                validatePublicKey(publicKey)
            } else {
                Toast.makeText(this, "Please enter a public key.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun redirectToLobstrWallet() {
        try {
            val lobstrUri = "lobstr://"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lobstrUri))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            startActivity(intent)
        } catch (e: Exception) {
            // If the app is not installed, redirects to the Play Store.
            val playStoreUri = "https://play.google.com/store/apps/details?id=com.lobstr.client"
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri))
            startActivity(fallbackIntent)
            Toast.makeText(this, "Please install Lobstr Wallet to continue.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showWalletSelectionDialog() {
        val walletSelectionDialog = WalletSelection { walletName ->
            lifecycleScope.launch {
                walletManager.connectWallet(walletName)
            }
        }
        walletSelectionDialog.show(supportFragmentManager, "WalletSelection")
    }

    private fun validatePublicKey(publicKey: String) {
        val url = "https://horizon-testnet.stellar.org/accounts/$publicKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@EscrowApproverActivity, "Error verifying the password.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        savePublicKey(publicKey)
                        navigateToFindEscrow(publicKey)
                    } else {
                        Toast.makeText(this@EscrowApproverActivity, "Invalid public key.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun savePublicKey(publicKey: String) {
        val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("publicKey", publicKey)
            .apply()
    }

    private fun navigateToFindEscrow(publicKey: String) {
        val intent = Intent(this, FindEscrowActivity::class.java)
        intent.putExtra("publicKey", publicKey)
        startActivity(intent)
        finish()
    }
}
