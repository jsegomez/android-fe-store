package com.jsegomez.store.features.products.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsegomez.store.features.products.data.InMemoryProductRepository
import com.jsegomez.store.features.products.domain.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProductListViewModel(
    repository: ProductRepository = InMemoryProductRepository
) : ViewModel() {
    val uiState: StateFlow<ProductListUiState> = repository.products
        .map { ProductListUiState(products = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductListUiState(products = repository.products.value)
        )
}
