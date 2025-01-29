package com.example.fideicomisoapproverring

import ConnectingDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import java.io.IOException

class EscrowApproverActivity : AppCompatActivity() {

    private lateinit var connectButton: Button
    private lateinit var validateKeyButton: Button
    private lateinit var publicKeyInput: TextInputEditText
    private lateinit var progressDialog: ConnectingDialog
    private lateinit var statusBanner: RelativeLayout
    private lateinit var statusIcon: ImageView
    private lateinit var statusText: TextView

    private var connectionStatus: ConnectionStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escrow_approver)


        connectButton = findViewById(R.id.connectButton)
        validateKeyButton = findViewById(R.id.validatePublicKeyButton)
        publicKeyInput = findViewById(R.id.publicKeyInput)
        progressDialog = ConnectingDialog(this)
        statusBanner = findViewById(R.id.statusBanner)
        statusIcon = findViewById(R.id.statusIcon)
        statusText = findViewById(R.id.statusText)


        statusBanner.visibility = View.GONE

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
                progressDialog.show()
                validatePublicKey(publicKey)
            } else {
                Toast.makeText(this, "Please enter a public key.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validatePublicKey(publicKey: String) {
        val url = "https://horizon-testnet.stellar.org/accounts/$publicKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        showStatusBanner(ConnectionStatus.ERROR, "") // ✅ Pasamos un string vacío
                    }, 3000)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        val publicKeyValue = publicKeyInput.text.toString()

                        if (response.isSuccessful && publicKeyValue.isNotEmpty()) {
                            savePublicKey(publicKeyValue)
                            navigateToFindEscrow(publicKeyValue, ConnectionStatus.SUCCESS)
                        } else {
                            showStatusBanner(ConnectionStatus.ERROR, publicKeyValue) // ✅ Pasamos la publicKey si existe
                        }
                    }, 3000)
                }
            }
        })

    }


    private fun showStatusBanner(status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.SUCCESS -> {
                statusBanner.setBackgroundResource(R.drawable.toast_success)
                statusIcon.setImageResource(R.drawable.check)
                statusText.text = "Connection Successfully"

                Handler(Looper.getMainLooper()).postDelayed({
                    statusBanner.visibility = View.GONE
                    navigateToFindEscrow(status)
                }, 3000)
            }
            ConnectionStatus.WARNING -> {
                statusBanner.setBackgroundResource(R.drawable.toast_warning)
                statusIcon.setImageResource(R.drawable.info)
                statusText.text = "Action Required: Please check your input."
                statusBanner.visibility = View.VISIBLE
            }
            ConnectionStatus.ERROR -> {
                statusBanner.setBackgroundResource(R.drawable.toast_error)
                statusIcon.setImageResource(R.drawable.cancel)
                statusText.text = "Error: Unable to connect."
                statusBanner.visibility = View.VISIBLE

                // Ocultar error después de 5 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    statusBanner.visibility = View.GONE
                }, 5000)
            }
        }
    }




    private fun showWalletSelectionDialog() {
        val walletSelectionDialog = WalletSelection { walletName ->
            Toast.makeText(this, "Selected wallet: $walletName", Toast.LENGTH_SHORT).show()
        }
        walletSelectionDialog.show(supportFragmentManager, "WalletSelection")
    }

    private fun savePublicKey(publicKey: String) {
        val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("publicKey", publicKey).apply()
    }

    private fun navigateToFindEscrow(publicKey: String, status: ConnectionStatus) {
        val intent = Intent(this, FindEscrowActivity::class.java)
        intent.putExtra("publicKey", publicKey)
        intent.putExtra("connectionStatus", status.name) // Pasamos el estado como String
        startActivity(intent)
        finish()
    }

}
