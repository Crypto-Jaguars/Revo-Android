package com.example.fideicomisoapproverring

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.identity.util.UUID
import com.example.fideicomisoapproverring.security.SecureWalletSessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class WalletSelection(private val onWalletSelected: (String) -> Unit) :
    BottomSheetDialogFragment() {

    private val TAG = "Session"
    private lateinit var sessionManager: SecureWalletSessionManager

    companion object {
        private const val LOBSTR_PACKAGE = "com.lobstr.client"
        private const val LOBSTR_URI_SCHEME = "lobstr://"
        private val LOBSTR_SIGNATURE_HASH = BuildConfig.LOBSTR_SIGNATURE_HASH
        private const val MAX_TIMESTAMP_DIFF = 5 * 60 * 1000 // 5 minutes
    }

    data class WalletOption(val name: String, val iconRes: Int, val isAvailable: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sessionManager = SecureWalletSessionManager(requireContext())

        val view = inflater.inflate(R.layout.activity_fragment_wallet_selection, container, false)


        val qrButton = view.findViewById<TextView>(R.id.viewQRText)
        qrButton.setOnClickListener {
            Toast.makeText(context, "QR Code feature coming soon!", Toast.LENGTH_SHORT).show()
        }


        val walletList = view.findViewById<RecyclerView>(R.id.walletList)
        walletList.layoutManager = LinearLayoutManager(context)

        val wallets = listOf(
            WalletOption("xBull", R.drawable.xbulllogo, true),
            WalletOption("Albedo", R.drawable.albedologo, true),
            WalletOption("LOBSTR", R.drawable.lobstrlogo, true),
            WalletOption("Freighter", R.drawable.freigtherlogo, false),
            WalletOption("Rabet", R.drawable.rabetlogo, false),
            WalletOption("Hana Wallet", R.drawable.hanalogo, false)
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
                Toast.makeText(context, "${wallet.name} is not available.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view


    }

    private fun redirectToLobstrWallet() {
        try {
            if (!validateSecurityRequirements()) {
                showSecurityAlert("Security verification failed")
                return
            }

            val secureUri = buildSecureWalletUri() ?: run {
                showSecurityAlert("Invalid wallet configuration")
                return
            }

            launchWalletWithSecurity(secureUri)

        } catch (e: Exception) {
            Log.e(TAG, "Security error: ${e.message}")
            showSecurityAlert("Security check failed")
        }
    }

    private fun validateSecurityRequirements(): Boolean {
        try {
            val packageManager = requireContext().packageManager
            val packageInfo = packageManager.getPackageInfo(
                LOBSTR_PACKAGE,
                PackageManager.GET_SIGNATURES or PackageManager.GET_META_DATA
            )


            if (packageInfo == null) {
                Log.e(TAG, "Package validation failed: Package not found")
                return false
            }


            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageInfo.signingInfo
                signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                Log.e(TAG, "Package validation failed: No signatures found")
                return false
            }


            val signatureHash = calculateSignatureHash(signatures[0])
            if (signatureHash != LOBSTR_SIGNATURE_HASH) {
                Log.e(TAG, "Package validation failed: Invalid signature")
                return false
            }

         
            if (!validateInstallSource(packageManager, LOBSTR_PACKAGE)) {
                Log.e(TAG, "Package validation failed: Invalid install source")
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Security validation error: ${e.message}")
            return false
        }
    }

    private fun buildSecureWalletUri(): Uri? {
        try {
            val timestamp = System.currentTimeMillis()
            val nonce = generateSecureNonce()

            val baseUri = Uri.parse(LOBSTR_URI_SCHEME)
            if (baseUri.scheme != "lobstr") {
                Log.e(TAG, "URI validation failed: Invalid scheme")
                return null
            }

            return baseUri.buildUpon()
                .appendPath("connect")
                .appendQueryParameter("app", "fideicomiso_approver")
                .appendQueryParameter("nonce", nonce)
                .appendQueryParameter("timestamp", timestamp.toString())
                .appendQueryParameter("version", BuildConfig.VERSION_NAME)
                .appendQueryParameter("signature", generateRequestSignature(nonce, timestamp))
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "URI building error: ${e.message}")
            return null
        }
    }

    private fun launchWalletWithSecurity(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(LOBSTR_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }


            if (!isIntentResolvable(intent)) {
                redirectToPlayStore()
                return
            }

            startActivity(intent)
            Log.d(TAG, "Wallet launched with secure parameters")

        } catch (e: Exception) {
            Log.e(TAG, "Wallet launch error: ${e.message}")
            redirectToPlayStore()
        }
    }

    private fun validateInstallSource(pm: PackageManager, packageName: String): Boolean {
        return try {
            val installSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }


            val validSources = listOf(
                "com.android.vending",
                "com.google.android.packageinstaller"
            )

            validSources.contains(installSource)
        } catch (e: Exception) {
            Log.e(TAG, "Install source validation failed: ${e.message}")
            false
        }
    }

    private fun generateSecureNonce(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateRequestSignature(nonce: String, timestamp: Long): String {
        val data = "$nonce:$timestamp:${BuildConfig.APP_SECRET_KEY}"
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(BuildConfig.APP_SECRET_KEY.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        return mac.doFinal(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun calculateSignatureHash(signature: Signature): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(signature.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun isIntentResolvable(intent: Intent): Boolean {
        return requireContext().packageManager.queryIntentActivities(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        ).isNotEmpty()
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
            val playStoreUri = "https://play.google.com/store/apps/details?id=com.lobstr.client"
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri))
            startActivity(fallbackIntent)
            Toast.makeText(context, "Please install Lobstr Wallet to continue.", Toast.LENGTH_LONG)
                .show()
        }
    }
}


