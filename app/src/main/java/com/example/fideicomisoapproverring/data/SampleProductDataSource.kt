package com.example.fideicomisoapproverring.data

import com.example.fideicomisoapproverring.models.Product

class SampleProductDataSource : ProductDataSource {
    private val sampleProducts = listOf(
        Product(
            id = "1",
            name = "Organic Tomatoes",
            description = "Fresh organic tomatoes from local farm",
            price = 4.99,
            imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer123",
            certifications = listOf("organic", "fair_trade")
        ),
        Product(
            id = "2",
            name = "Fresh Sweet Corn",
            description = "Sweet corn harvested today",
            price = 3.49,
            imageUrl = "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer456",
            certifications = listOf("organic")
        ),
        Product(
            id = "3",
            name = "Rainbow Carrots",
            description = "Organic rainbow carrots, freshly harvested",
            price = 3.99,
            imageUrl = "https://images.unsplash.com/photo-1582515073490-39981397c445?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer789",
            certifications = listOf("organic")
        ),
        Product(
            id = "4",
            name = "Fresh Strawberries",
            description = "Sweet and juicy strawberries",
            price = 5.99,
            imageUrl = "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer123",
            certifications = listOf("organic", "fair_trade")
        ),
        Product(
            id = "5",
            name = "Bell Peppers Mix",
            description = "Colorful mix of fresh bell peppers",
            price = 4.49,
            imageUrl = "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer456",
            certifications = listOf("fair_trade")
        ),
        Product(
            id = "6",
            name = "Fresh Basil",
            description = "Aromatic fresh basil leaves",
            price = 2.99,
            imageUrl = "https://images.unsplash.com/photo-1618164435735-413d3b066c9a?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer789",
            certifications = listOf("organic")
        ),
        Product(
            id = "7",
            name = "Red Potatoes",
            description = "Fresh red potatoes, perfect for roasting",
            price = 3.49,
            imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer123",
            certifications = listOf("fair_trade")
        ),
        Product(
            id = "8",
            name = "Fresh Spinach",
            description = "Organic baby spinach leaves",
            price = 3.99,
            imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer456",
            certifications = listOf("organic", "fair_trade")
        ),
        Product(
            id = "9",
            name = "Honey Crisp Apples",
            description = "Sweet and crispy apples",
            price = 4.99,
            imageUrl = "https://images.unsplash.com/photo-1570913149827-d2ac84ab3f9a?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer789",
            certifications = listOf("organic")
        ),
        Product(
            id = "10",
            name = "Cherry Tomatoes",
            description = "Sweet cherry tomatoes",
            price = 3.99,
            imageUrl = "https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer123",
            certifications = listOf("organic", "fair_trade")
        ),
        Product(
            id = "11",
            name = "Fresh Avocados",
            description = "Creamy, ripe avocados",
            price = 6.99,
            imageUrl = "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer456",
            certifications = listOf("organic")
        ),
        Product(
            id = "12",
            name = "Purple Eggplant",
            description = "Fresh, glossy eggplants",
            price = 3.99,
            imageUrl = "https://images.unsplash.com/photo-1615484477778-ca3b77940c25?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer789",
            certifications = listOf("organic", "fair_trade")
        ),
        Product(
            id = "13",
            name = "Fresh Broccoli",
            description = "Crisp, green broccoli crowns",
            price = 2.99,
            imageUrl = "https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer123",
            certifications = listOf("organic")
        ),
        Product(
            id = "14",
            name = "Sweet Blueberries",
            description = "Plump, sweet blueberries",
            price = 5.99,
            imageUrl = "https://images.unsplash.com/photo-1498557850523-fd3d118b962e?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer456",
            certifications = listOf("organic", "fair_trade")
        ),
        Product(
            id = "15",
            name = "Fresh Asparagus",
            description = "Tender green asparagus spears",
            price = 4.99,
            imageUrl = "https://images.unsplash.com/photo-1515471209610-dae1c92d8777?w=500&q=80",
            isAvailable = true,
            farmerId = "farmer789",
            certifications = listOf("organic")
        )
    ).associateBy { it.id }

    override suspend fun getProducts(page: Int, pageSize: Int): Result<List<Product>> = runCatching {
        require(page > 0) { "Page must be greater than 0" }
        require(pageSize > 0) { "Page size must be greater than 0" }
        
        val totalProducts = sampleProducts.size
        require((page - 1) * pageSize < totalProducts) { "Page number exceeds available products" }
        
        sampleProducts.values
            .drop((page - 1) * pageSize)
            .take(pageSize)
            .toList()
    }

    override suspend fun getProductById(id: String): Result<Product> = runCatching {
        requireNotNull(sampleProducts[id]) { "Product not found with id: $id" }
    }
} 