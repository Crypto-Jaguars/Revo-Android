package com.example.fideicomisoapproverring.models

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val isAvailable: Boolean,
    val farmerId: String,
    val certifications: List<String> = emptyList()
) {
    val isLoading: Boolean
        get() = id == "loading"

    companion object {
        val LoadingItem = Product(
            id = "loading",
            name = "",
            description = "",
            price = 0.0,
            imageUrl = "",
            isAvailable = false,
            farmerId = "",
            certifications = emptyList()
        )
    }
} 