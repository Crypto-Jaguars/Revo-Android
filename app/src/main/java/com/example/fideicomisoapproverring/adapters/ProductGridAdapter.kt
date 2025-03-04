package com.example.fideicomisoapproverring.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.fideicomisoapproverring.R
import com.example.fideicomisoapproverring.models.Product
import com.example.fideicomisoapproverring.theme.utils.ImageUtils
import java.text.NumberFormat
import java.util.Locale

class ProductGridAdapter(
    private val context: Context,
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductGridAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val glideOptions = RequestOptions()
        .placeholder(R.drawable.product_placeholder)
        .error(R.drawable.product_error)

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val availability: TextView = itemView.findViewById(R.id.productAvailability)
        val actionButton: View = itemView.findViewById(R.id.actionButton)
        val certificationsContainer: ViewGroup = itemView.findViewById(R.id.certificationsContainer)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProductClick(getItem(position))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_grid, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        
        if (product.isLoading) {
            holder.apply {
                image.setImageResource(R.drawable.product_placeholder)
                name.text = ""
                price.text = ""
                availability.visibility = View.GONE
                actionButton.visibility = View.GONE
                certificationsContainer.visibility = View.GONE
                itemView.setOnClickListener(null)
            }
            return
        }

        // Preload next few images
        if (position <= itemCount - 4) {
            for (i in 1..3) {
                val nextPosition = position + i
                if (nextPosition < itemCount) {
                    val nextProduct = getItem(nextPosition)
                    Glide.with(context)
                        .load(nextProduct.imageUrl)
                        .apply(if (isDarkMode) ImageUtils.getDarkModeGlideOptions() else glideOptions)
                        .preload()
                }
            }
        }

        // Load current image
        Glide.with(context)
            .load(product.imageUrl)
            .apply(if (isDarkMode) ImageUtils.getDarkModeGlideOptions() else glideOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.image)
        
        holder.image.contentDescription = "Image of ${product.name}"
        holder.price.contentDescription = "Price: ${currencyFormat.format(product.price)}"
        holder.itemView.contentDescription = "${product.name}, ${product.description}, " +
            "Price: ${currencyFormat.format(product.price)}, " +
            if (product.isAvailable) "In Stock" else "Out of Stock"
        
        holder.name.text = product.name
        holder.price.text = currencyFormat.format(product.price)
        
        holder.availability.visibility = if (product.isAvailable) View.VISIBLE else View.GONE
        holder.actionButton.visibility = if (product.isAvailable) View.VISIBLE else View.GONE
        
        // Handle certifications
        holder.certificationsContainer.removeAllViews()
        if (product.certifications.isNotEmpty()) {
            holder.certificationsContainer.visibility = View.VISIBLE
            product.certifications.forEach { certification ->
                val textView = TextView(context).apply {
                    text = certification
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
                    setPadding(8, 4, 8, 4)
                    setBackgroundResource(R.drawable.bg_certification)
                }
                holder.certificationsContainer.addView(textView)
            }
        } else {
            holder.certificationsContainer.visibility = View.GONE
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 