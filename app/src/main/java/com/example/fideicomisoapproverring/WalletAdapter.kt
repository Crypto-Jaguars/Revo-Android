// WalletAdapter.kt
package com.example.fideicomisoapproverring

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WalletAdapter(
    private val walletOptions: List<WalletSelection.WalletOption>,
    private val onWalletSelected: (WalletSelection.WalletOption) -> Unit
) : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val walletName: TextView = itemView.findViewById(R.id.walletName)
        val walletStatus: TextView = itemView.findViewById(R.id.walletStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_wallet_item, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = walletOptions[position]
        holder.walletName.text = wallet.name
        holder.walletStatus.text = if (wallet.isAvailable) "" else "Not available"
        holder.walletStatus.visibility = if (wallet.isAvailable) View.GONE else View.VISIBLE

        // Change color according to availability
        holder.walletName.setTextColor(
            if (wallet.isAvailable) holder.itemView.context.getColor(android.R.color.white)
            else holder.itemView.context.getColor(android.R.color.darker_gray)
        )

        holder.itemView.setOnClickListener { onWalletSelected(wallet) }
    }

    override fun getItemCount(): Int = walletOptions.size
}
