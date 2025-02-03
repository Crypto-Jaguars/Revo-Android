package com.example.fideicomisoapproverring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WalletSelection(private val onWalletSelected: (String) -> Unit) :
    BottomSheetDialogFragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            val lobstrUri = "lobstr://"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lobstrUri))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            startActivity(intent)
        } catch (e: Exception) {
            val playStoreUri = "https://play.google.com/store/apps/details?id=com.lobstr.client"
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUri))
            startActivity(fallbackIntent)
            Toast.makeText(context, "Please install Lobstr Wallet to continue.", Toast.LENGTH_LONG)
                .show()
        }
    }


    data class WalletOption(val name: String, val iconRes: Int, val isAvailable: Boolean)
}


