package com.example.fideicomisoapproverring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
                if (walletName == "LOBSTR") {
                    connectToLobstrWallet() // Redirects to Lobstr app
                } else {
                    simulateWalletConnection(walletName) // Simulates connection to other wallets
                }
            }
            bottomSheet.show(supportFragmentManager, "WalletSelection")
        }
    }

    // Redirects to LOBSTR Wallet if installed, otherwise, open Play Store
    private fun connectToLobstrWallet() {
        try {
            val lobstrUrl = "lobstr://" // Diagram to open the Lobstr Wallet app
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lobstrUrl))
            startActivity(intent)
        } catch (e: Exception) {
            // If Lobstr is not installed, redirect to the Play Store.
            val playStoreUrl = "https://play.google.com/store/apps/details?id=com.lobstr.client"
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            startActivity(playStoreIntent)
            Toast.makeText(this, "Please install LOBSTR Wallet to continue.", Toast.LENGTH_LONG).show()
        }
    }

    // Simulates a connection for other unsupported wallets
    private fun simulateWalletConnection(walletName: String) {
        Toast.makeText(this, "Simulated connection with $walletName.", Toast.LENGTH_SHORT).show()
    }
}
