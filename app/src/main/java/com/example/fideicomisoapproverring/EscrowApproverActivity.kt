package com.example.fideicomisoapproverring

import ConnectingDialog
import android.content.Intent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
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

    private var connectionStatus: ConnectionStatus? = null // Estado de conexión

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escrow_approver)

        connectButton = findViewById(R.id.connectButton)
        validateKeyButton = findViewById(R.id.validatePublicKeyButton)
        publicKeyInput = findViewById(R.id.publicKeyInput)
        progressDialog = ConnectingDialog(this)

        publicKeyInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateKeyButton.isEnabled = !s.isNullOrEmpty()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        connectButton.setOnClickListener {
            showWalletSelectionDialog() // ⬅️ Vuelve a estar aquí
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

    private fun showWalletSelectionDialog() {
        val walletSelectionDialog = WalletSelection { walletName ->
            Toast.makeText(this, "Selected wallet: $walletName", Toast.LENGTH_SHORT).show()
            if (walletName == "LOBSTR") {
                // Si se selecciona LOBSTR, se redirige automáticamente
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
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        connectionStatus = ConnectionStatus.ERROR // ⬅️ Mostrar banner de error
                        showStatusBanner()
                    }, 3000)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            connectionStatus = ConnectionStatus.SUCCESS // ⬅️ Mostrar banner de éxito
                            showStatusBanner()
                            savePublicKey(publicKey)
                            navigateToFindEscrow(publicKey)
                        } else {
                            connectionStatus = ConnectionStatus.ERROR // ⬅️ Mostrar banner de error
                            showStatusBanner()
                        }
                    }, 3000)
                }
            }
        })
    }

    private fun showStatusBanner() {
        val message = when (connectionStatus) {
            ConnectionStatus.SUCCESS -> "Connected Successfully!"
            ConnectionStatus.ERROR -> "Error: Unable to connect."
            else -> return
        }

        val toastColor = when (connectionStatus) {
            ConnectionStatus.SUCCESS -> "#1DB954"
            ConnectionStatus.ERROR -> "#FF4444"
            else -> "#FFFFFF"
        }

        runOnUiThread {
            val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
            toast.view?.setBackgroundColor(android.graphics.Color.parseColor(toastColor))
            toast.show()
        }
    }

    private fun savePublicKey(publicKey: String) {
        val sharedPreferences = getSharedPreferences("WalletPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putString("publicKey", publicKey).apply()
    }

    private fun navigateToFindEscrow(publicKey: String) {
        val intent = Intent(this, FindEscrowActivity::class.java)
        intent.putExtra("publicKey", publicKey)
        startActivity(intent)
        finish()
    }
}
