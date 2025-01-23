package com.example.fideicomisoapproverring.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val isAvailable: Boolean,
    val farmerId: String,
    val certifications: List<String>,
    val isLoading: Boolean = false
) : Parcelable {
    companion object {
        val LoadingItem = Product(
            id = "loading",
            name = "",
            description = "",
            price = 0.0,
            imageUrl = "",
            isAvailable = false,
            farmerId = "",
            certifications = emptyList(),
            isLoading = true
        )
    }
} 