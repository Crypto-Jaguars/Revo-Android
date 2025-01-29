
package com.example.fideicomisoapproverring

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class WalletAdapter(
    private val walletOptions: List<WalletSelection.WalletOption>,
    private val onWalletSelected: (WalletSelection.WalletOption) -> Unit
) : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val walletIcon: ImageView = itemView.findViewById(R.id.walletIcon)
        val walletName: TextView = itemView.findViewById(R.id.walletName)
        val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_wallet_item, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = walletOptions[position]

        holder.walletIcon.setImageResource(wallet.iconRes)
        holder.walletName.text = wallet.name
        holder.arrowIcon.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            if (wallet.isAvailable) {
                onWalletSelected(wallet)
            } else {
                Toast.makeText(holder.itemView.context, "${wallet.name} is not available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = walletOptions.size
}


