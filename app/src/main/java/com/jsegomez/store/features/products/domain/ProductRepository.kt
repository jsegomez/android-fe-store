package com.jsegomez.store.features.products.domain

import com.jsegomez.store.features.products.domain.model.Product
import kotlinx.coroutines.flow.StateFlow

interface ProductRepository {
    val products: StateFlow<List<Product>>

    fun addProduct(
        title: String,
        price: Double,
        description: String,
        category: String,
        image: String
    )
}
