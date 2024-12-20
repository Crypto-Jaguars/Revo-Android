package com.example.fideicomisoapproverring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EscrowApproverActivity : AppCompatActivity() {

    private lateinit var connectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escrow_approver)

        connectButton = findViewById(R.id.connectButton)
        connectButton.setOnClickListener {
            val bottomSheet = WalletSelectionBottomSheet { walletName ->
                simulateWalletConnection(walletName) // Simulate connection
            }
            bottomSheet.show(supportFragmentManager, "WalletSelection")
        }


        checkExistingConnection() // Verify existing connection when starting the app
    }


    // Checks for an existing connection and redirects if valid
    private fun checkExistingConnection() {
        val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
        val isWalletConnected = sharedPreferences.getBoolean("isWalletConnected", false)
        val publicKey = sharedPreferences.getString("publicKey", null)

        if (isWalletConnected && publicKey != null) {
            // Validate connection (can be real or simulated)
            val isValidConnection = validateConnection(publicKey)
            if (isValidConnection) {
                navigateToFindEscrow(publicKey)
            } else {
                resetConnectionState()
            }
        }
    }

    // Validates if the connection is valid (simulation)
    private fun validateConnection(publicKey: String): Boolean {
        return publicKey.startsWith("SIMULATED_PUBLIC_KEY")
    }

    // Simulates connection to a selected wallet
    private fun simulateWalletConnection(walletName: String) {
        Toast.makeText(this, "Connecting to $walletName...", Toast.LENGTH_SHORT).show()

        val publicKey = "SIMULATED_PUBLIC_KEY_$walletName" // Simulated public key
        // Save connection status
        val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("isWalletConnected", true)
            .putString("publicKey", publicKey)
            .apply()

        Log.d("EscrowApproverActivity", "Connected to $walletName with key: $publicKey")
        navigateToFindEscrow(publicKey)
    }

    // Resets the connection status
    private fun resetConnectionState() {
        val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("isWalletConnected")
            .remove("publicKey")
            .apply()
    }

    // Redirects to FindEscrow screen
    private fun navigateToFindEscrow(publicKey: String) {
        val intent = Intent(this, FindEscrowActivity::class.java)
        intent.putExtra("publicKey", publicKey)
        startActivity(intent)
        finish()
    }

    // Example of future function for a real integration with Albedo
    private fun connectToAlbedo() {

        val uri = Uri.parse("https://albedo.link/intro")  // Here you could integrate a real call to Albedo using an Intent or SDK.
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)

        // NOTE: Once the user returns, capture the actual result.
        // using a callback or validating the public key.
    }
}
