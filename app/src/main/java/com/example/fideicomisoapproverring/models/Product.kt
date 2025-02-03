package com.example.fideicomisoapproverring.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val imageUrl: String,
    val isAvailable: Boolean,
    val farmerId: String,
    val certifications: List<String> = emptyList(),
    val isLoading: Boolean = false
) : Parcelable {
    companion object {
        val LoadingItem = Product(
            id = "loading",
            name = "",
            description = "",
            price = BigDecimal.ZERO,
            imageUrl = "",
            isAvailable = false,
            farmerId = "",
            certifications = emptyList(),
            isLoading = true
        )
    }
} 