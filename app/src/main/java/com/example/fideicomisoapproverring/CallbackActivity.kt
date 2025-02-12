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
        val publicKey = data?.getQueryParameter("pubkey") // Obtains the public key

        if (publicKey != null) {
            // Save public key in SharedPreferences
            val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean("isWalletConnected", true)
                .putString("publicKey", publicKey)
                .apply()

            Toast.makeText(this, "Public key received: $publicKey", Toast.LENGTH_SHORT).show()

            // Redirect to FindEscrow
            val intent = Intent(this, FindEscrowActivity::class.java)
            intent.putExtra("publicKey", publicKey)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Error: Public key not received.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
