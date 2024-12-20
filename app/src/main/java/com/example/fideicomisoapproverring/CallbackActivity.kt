package com.example.fideicomisoapproverring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class CallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data
        if (data != null) {
            val publicKey = data.getQueryParameter("pubkey") // Receives the public key
            if (publicKey != null) {
                // Save connection status
                val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
                sharedPreferences.edit()
                    .putBoolean("isWalletConnected", true)
                    .putString("publicKey", publicKey)
                    .apply()

                // Redirect the user
                val intent = Intent(this, FindEscrowActivity::class.java)
                intent.putExtra("publicKey", publicKey)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error: Public key not received.", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(this, "Error: No data received.", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}

