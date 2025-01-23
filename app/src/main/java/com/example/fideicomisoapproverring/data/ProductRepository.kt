package com.example.fideicomisoapproverring.data

import com.example.fideicomisoapproverring.models.Product

class ProductRepository(private val dataSource: ProductDataSource) {
    suspend fun getProducts(page: Int, pageSize: Int): Result<List<Product>> =
        dataSource.getProducts(page, pageSize)

    suspend fun getProductById(id: String): Result<Product> =
        dataSource.getProductById(id)

    companion object {
        @Volatile
        private var instance: ProductRepository? = null

        fun getInstance(): ProductRepository =
            instance ?: synchronized(this) {
                instance ?: ProductRepository(SampleProductDataSource()).also { instance = it }
            }
    }
} 