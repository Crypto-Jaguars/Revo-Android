package com.example.fideicomisoapproverring.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.button.MaterialButton
import com.example.fideicomisoapproverring.R
import com.example.fideicomisoapproverring.models.Product
import java.text.NumberFormat
import java.util.*

class ProductGridAdapter(
    private val context: Context,
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductGridAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private val glideOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.product_placeholder)
        .error(R.drawable.product_error)
        .centerCrop()
        .override(500, 500)
        .format(DecodeFormat.PREFER_RGB_565)
        .transform(RoundedCorners(12))

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("USD")
    }

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.productImage)
        val name: TextView = view.findViewById(R.id.productName)
        val price: TextView = view.findViewById(R.id.productPrice)
        val availability: TextView = view.findViewById(R.id.availabilityStatus)
        val actionButton: MaterialButton = view.findViewById(R.id.actionButton)
        val certificationsContainer: LinearLayout = view.findViewById(R.id.certificationsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_grid_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        
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
                        .apply(glideOptions)
                        .preload()
                }
            }
        }

        // Load current image
        Glide.with(context)
            .load(product.imageUrl)
            .apply(glideOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.image)
        
        holder.image.contentDescription = "Image of ${product.name}"
        holder.price.contentDescription = "Price: ${currencyFormat.format(product.price)}"
        holder.itemView.contentDescription = "${product.name}, ${product.description}, " +
            "Price: ${currencyFormat.format(product.price)}, " +
            if (product.isAvailable) "In Stock" else "Out of Stock"
        
        holder.name.text = product.name
        holder.price.text = currencyFormat.format(product.price)
        
        holder.availability.apply {
            text = if (product.isAvailable) "In Stock" else "Out of Stock"
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (product.isAvailable) android.R.color.holo_green_dark 
                    else android.R.color.holo_red_dark
                )
            )
        }

        bindCertifications(holder, product)

        // Set click listener with ripple effect
        holder.itemView.setOnClickListener { onProductClick(product) }
        holder.actionButton.setOnClickListener { onProductClick(product) }
    }

    private fun bindCertifications(holder: ProductViewHolder, product: Product) {
        holder.certificationsContainer.removeAllViews()
        if (product.certifications.isEmpty()) {
            holder.certificationsContainer.visibility = View.GONE
            return
        }
        holder.certificationsContainer.visibility = View.VISIBLE

        // Create a map of certification types to resource IDs
        val certificationResources = mapOf(
            "organic" to R.drawable.ic_organic_badge,
            "fair_trade" to R.drawable.ic_fair_trade_badge
        )

        product.certifications.forEach { certification ->
            try {
                val resourceId = certificationResources[certification.lowercase()]
                    ?: R.drawable.ic_certified_badge

                val badge = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = 8
                    }
                    setImageResource(resourceId)
                    contentDescription = certification
                }
                holder.certificationsContainer.addView(badge)
            } catch (e: Exception) {
                Log.e("ProductGridAdapter", "Failed to add certification badge: $certification", e)
                // Continue with next certification
                return@forEach
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem || (oldItem.isLoading && newItem.isLoading)
        }
    }
} 