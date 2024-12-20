package com.example.fideicomisoapproverring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WalletSelectionBottomSheet(private val onWalletSelected: (String) -> Unit) : BottomSheetDialogFragment() {

    data class WalletOption(val name: String, val isAvailable: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wallet_selection, container, false)

        val walletList = view.findViewById<RecyclerView>(R.id.walletList)
        walletList.layoutManager = LinearLayoutManager(context)

        val wallets = listOf(
            WalletOption("xBull", true),
            WalletOption("Albedo", true),
            WalletOption("Freighter", false),
            WalletOption("Rabet", false),
            WalletOption("LOBSTR", false),
            WalletOption("Hana Wallet", false)
        )

        walletList.adapter = WalletAdapter(wallets) { wallet ->
            if (wallet.isAvailable) {
                onWalletSelected(wallet.name)
                dismiss()
            } else {

                dismiss() // Simply close in simulation
            }
        }

        return view
    }
}
