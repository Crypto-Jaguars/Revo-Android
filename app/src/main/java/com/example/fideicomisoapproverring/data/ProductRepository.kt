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
        private var dataSource: ProductDataSource? = null

        fun getInstance(source: ProductDataSource? = null): ProductRepository =
            instance ?: synchronized(this) {
                if (source != null) {
                    dataSource = source
                }
                instance ?: ProductRepository(
                    dataSource ?: SampleProductDataSource()
                ).also { instance = it }
            }
    }
} 