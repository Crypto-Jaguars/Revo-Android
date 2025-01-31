package com.example.fideicomisoapproverring

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.identity.util.UUID
import com.example.fideicomisoapproverring.security.SecureWalletSessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.security.MessageDigest
//import java.security.Signature

class WalletSelection(private val onWalletSelected: (String) -> Unit) : BottomSheetDialogFragment() {

    private val TAG = "Session"
    data class WalletOption(val name: String, val isAvailable: Boolean)
    private lateinit var sessionManager: SecureWalletSessionManager

    companion object {
        private const val LOBSTR_PACKAGE = "com.lobstr.client"
        private const val LOBSTR_URI_SCHEME = "lobstr://"
        private const val LOBSTR_SIGNATURE_HASH = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sessionManager = SecureWalletSessionManager(requireContext())

        val view = inflater.inflate(R.layout.activity_fragment_wallet_selection, container, false)

        val walletList = view.findViewById<RecyclerView>(R.id.walletList)
        walletList.layoutManager = LinearLayoutManager(context)

        // Lista de wallets disponibles
        val wallets = listOf(
            WalletOption("xBull", true),
            WalletOption("Albedo", true),
            WalletOption("LOBSTR", true),
            WalletOption("Freighter", false),
            WalletOption("Rabet", false),
            WalletOption("Hana Wallet", false)
        )

        walletList.adapter = WalletAdapter(wallets) { wallet ->
            if (wallet.isAvailable) {

                val sessionToken = UUID.randomUUID().toString()
                val deviceId = android.provider.Settings.Secure.getString(
                    requireContext().contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )

                sessionManager.saveWalletSession(wallet.name, sessionToken, deviceId)
                onWalletSelected(wallet.name)
                if (wallet.name == "LOBSTR") {
                    redirectToLobstrWallet()
                }
                dismiss()
            } else {
                Toast.makeText(context, "${wallet.name} is not yet available.", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun redirectToLobstrWallet() {
        try {
            val packageManager = requireContext().packageManager
            if (!isWalletPackageValid(LOBSTR_PACKAGE)) {
                Log.e(TAG, "❌ Invalid or tampered wallet package")
                showSecurityAlert("Invalid wallet package detected")
                return
            }
            val lobstrUri = validateAndBuildUri() ?: return
            try {
                val intent = Intent(Intent.ACTION_VIEW, lobstrUri).apply {
                    setPackage(LOBSTR_PACKAGE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    putExtra("source", "fideicomiso_approver")
                    putExtra("timestamp", System.currentTimeMillis())
                }

                if (isIntentResolvable(intent)) {
                    startActivity(intent)
                    Log.d(TAG, "✅ Successfully redirected to Lobstr wallet")
                } else {
                    redirectToPlayStore()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error launching wallet: ${e.message}")
                redirectToPlayStore()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Security check failed: ${e.message}")
            showSecurityAlert("Security verification failed")
        }
    }
    private fun isWalletPackageValid(packageName: String): Boolean {
        try {
            val packageManager = requireContext().packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            return packageInfo != null

            // Commented out signature verification for now because its not available for lobstr from line 119 to line 132
            /*
            @Suppress("DEPRECATION")
            val signatures = packageInfo.signatures
            if (signatures.isNullOrEmpty()) {
                Log.e(TAG, "❌ No package signatures found")
                return false
            }

            val signatureHash = calculateSignatureHash(signatures[0])
            if (signatureHash != LOBSTR_SIGNATURE_HASH) {
                Log.e(TAG, "❌ Invalid package signature")
                return false
            }
            */
            // Commented out signature verification for now because its not available for lobstr from line 119 to line 132
        } catch (e: Exception) {
            Log.e(TAG, "❌ Package validation failed: ${e.message}")
            return false
        }
    }

    private fun validateAndBuildUri(): Uri? {
        try {
            val uriString = LOBSTR_URI_SCHEME
            val uri = Uri.parse(uriString)
            if (uri.scheme != "lobstr") {
                Log.e(TAG, "❌ Invalid URI scheme")
                return null
            }
            val secureUriBuilder = uri.buildUpon()
                .appendQueryParameter("source", "fideicomiso_approver")
                .appendQueryParameter("timestamp", System.currentTimeMillis().toString())
                .appendQueryParameter("nonce", generateNonce())

            Log.d(TAG, "✅ URI validation successful")
            return secureUriBuilder.build()
        } catch (e: Exception) {
            Log.e(TAG, "❌ URI validation failed: ${e.message}")
            return null
        }
    }

    private fun isIntentResolvable(intent: Intent): Boolean {
        return requireContext().packageManager.queryIntentActivities(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        ).isNotEmpty()
    }

    private fun calculateSignatureHash(signature: Signature): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(signature.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun generateNonce(): String {
        return UUID.randomUUID().toString()
    }

    private fun redirectToPlayStore() {
        val playStoreUri = "https://play.google.com/store/apps/details?id=$LOBSTR_PACKAGE"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Toast.makeText(context, "Please install Lobstr Wallet to continue.", Toast.LENGTH_LONG).show()
    }

    private fun showSecurityAlert(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Security Warning")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun handleWalletCallback(publicKey: String) {
        if (sessionManager.updateWalletAddress(publicKey)) {
            Log.d(TAG, "Wallet address updated successfully")
        } else {
            Log.e(TAG, "Failed to update wallet address")
        }
    }
}
