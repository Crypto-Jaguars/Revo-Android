package com.example.fideicomisoapproverring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.identity.util.UUID
import com.example.fideicomisoapproverring.security.SecureWalletSessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WalletSelection(private val onWalletSelected: (String) -> Unit) : BottomSheetDialogFragment() {

    data class WalletOption(val name: String, val isAvailable: Boolean)
    private lateinit var sessionManager: SecureWalletSessionManager

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
            val lobstrPackage = "com.lobstr.client"

            try {
                packageManager.getPackageInfo(lobstrPackage, 0)
                // Lobstr is installed, proceed with deep link
                val lobstrUri = "lobstr://"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lobstrUri))
                intent.setPackage(lobstrPackage)  // Explicitly set package
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                // Lobstr not installed, redirect to Play Store
                val playStoreUri = "https://play.google.com/store/apps/details?id=com.lobstr.client"
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri))
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(fallbackIntent)
                Toast.makeText(context, "Please install Lobstr Wallet to continue.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error launching Lobstr wallet: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
