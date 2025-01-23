package com.example.fideicomisoapproverring.data

import com.example.fideicomisoapproverring.models.Product

interface ProductDataSource {
    suspend fun getProducts(page: Int, pageSize: Int): Result<List<Product>>
    suspend fun getProductById(id: String): Result<Product>
} 