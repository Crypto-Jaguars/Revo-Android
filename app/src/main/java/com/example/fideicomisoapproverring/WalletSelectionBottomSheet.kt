package com.example.fideicomisoapproverring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WalletSelectionBottomSheet(private val onWalletSelected: (String) -> Unit) : BottomSheetDialogFragment() {

    data class WalletOption(val name: String, val isAvailable: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_fragment_wallet_selection, container, false)

        val walletList = view.findViewById<RecyclerView>(R.id.walletList)
        walletList.layoutManager = LinearLayoutManager(context)

        // List of available wallets
        val wallets = listOf(
            WalletOption("xBull", true),
            WalletOption("Albedo", true),
            WalletOption("LOBSTR", true), // Habilitamos LOBSTR
            WalletOption("Freighter", false),
            WalletOption("Rabet", false),
            WalletOption("Hana Wallet", false)
        )

        walletList.adapter = WalletAdapter(wallets) { wallet ->
            if (wallet.isAvailable) {
                onWalletSelected(wallet.name)
                if (wallet.name == "LOBSTR") {
                    try {
                        val lobstrUrl = "lobstr://" // Explicit intent for LOBSTR
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lobstrUrl))
                        startActivity(intent)
                    } catch (e: Exception) {
                        // If not installed, redirects to Play Store
                        val fallbackUrl = "https://play.google.com/store/apps/details?id=com.lobstr.client"
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                        startActivity(fallbackIntent)
                        Toast.makeText(context, "Please install LOBSTR Wallet to continue.", Toast.LENGTH_LONG).show()
                    }
                }
                dismiss()
            } else {
                Toast.makeText(context, "${wallet.name} is not yet available.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
