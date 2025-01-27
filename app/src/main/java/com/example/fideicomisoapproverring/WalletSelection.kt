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
import java.net.URLEncoder

class WalletSelection(private val onWalletSelected: (String) -> Unit) : BottomSheetDialogFragment() {

    data class WalletOption(val name: String, val isAvailable: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            val lobstrUri = "lobstr://sign?" +
                "xdr=${URLEncoder.encode(challengeTx, "UTF-8")}&" +
                "callback=fideicomisoapproverring://wallet-callback&" +
                "pubkey=${URLEncoder.encode(sourceAccount, "UTF-8")}"
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lobstrUri))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            startActivity(intent)
        } catch (e: Exception) {
            val playStoreUri = "https://play.google.com/store/apps/details?id=com.lobstr.client"
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri))
            startActivity(fallbackIntent)
            Toast.makeText(context, "Please install Lobstr Wallet to continue.", Toast.LENGTH_LONG).show()
        }
    }
}
