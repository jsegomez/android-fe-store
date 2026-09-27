package com.jsegomez.store.features.products.presentation.list

import com.jsegomez.store.features.products.domain.model.Product

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && products.isEmpty()
}
