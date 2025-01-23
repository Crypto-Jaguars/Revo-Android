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
) 