package com.jsegomez.store.features.products.data

import com.jsegomez.store.features.products.domain.ProductRepository
import com.jsegomez.store.features.products.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Los productos viven solo en memoria: se pierden al cerrar la app
object InMemoryProductRepository : ProductRepository {
    private val _products = MutableStateFlow(FakeProducts.products)
    override val products: StateFlow<List<Product>> = _products.asStateFlow()

    override fun addProduct(
        title: String,
        price: Double,
        description: String,
        category: String,
        image: String
    ) {
        _products.update { current ->
            val newProduct = Product(
                id = (current.maxOfOrNull { it.id } ?: 0) + 1,
                title = title,
                price = price,
                description = description,
                category = category,
                image = image
            )
            // El nuevo producto se muestra primero en la lista
            listOf(newProduct) + current
        }
    }
}
