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
        val walletIcon: ImageView = itemView.findViewById(R.id.walletIcon) // Ícono de wallet
        val loadingAnimation: LottieAnimationView = itemView.findViewById(R.id.loadingAnimation) // Lottie para carga
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_wallet_item, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = walletOptions[position]

        // Configura el nombre y estado de la wallet
        holder.walletName.text = wallet.name
        holder.walletStatus.text = if (wallet.isAvailable) "" else "Not available"
        holder.walletStatus.visibility = if (wallet.isAvailable) View.GONE else View.VISIBLE

        // Configura el ícono de wallet
        when (wallet.name) {
            "xBull" -> holder.walletIcon.setImageResource(R.drawable.ic_xbull)
            "Albedo" -> holder.walletIcon.setImageResource(R.drawable.ic_albedo)
            "LOBSTR" -> holder.walletIcon.setImageResource(R.drawable.ic_lobstr)
            else -> holder.walletIcon.setImageResource(R.drawable.ic_wallet_generic)
        }

        // Cambia el color del texto según el estado
        holder.walletName.setTextColor(
            if (wallet.isAvailable) holder.itemView.context.getColor(R.color.green)
            else holder.itemView.context.getColor(R.color.red)
        )

        // Muestra animación de carga si la wallet no está disponible
        holder.loadingAnimation.visibility = if (wallet.isAvailable) View.GONE else View.VISIBLE

        // Maneja clics en el item
        holder.itemView.setOnClickListener {
            if (wallet.isAvailable) {
                onWalletSelected(wallet) // Llama al callback
            } else {
                Toast.makeText(holder.itemView.context, "${wallet.name} is not yet available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = walletOptions.size
}

