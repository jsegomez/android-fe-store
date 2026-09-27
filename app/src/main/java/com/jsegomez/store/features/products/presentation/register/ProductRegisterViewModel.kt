package com.jsegomez.store.features.products.presentation.register

import androidx.lifecycle.ViewModel
import com.jsegomez.store.features.products.data.InMemoryProductRepository
import com.jsegomez.store.features.products.domain.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductRegisterViewModel(
    private val repository: ProductRepository = InMemoryProductRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductRegisterUiState())
    val uiState: StateFlow<ProductRegisterUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onPriceChange(price: String) {
        _uiState.update { it.copy(price = price) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onImageChange(image: String) {
        _uiState.update { it.copy(image = image) }
    }

    fun onSaveClick(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isFormValid || state.isLoading) return
        repository.addProduct(
            title = state.title.trim(),
            price = state.price.toDouble(),
            description = state.description.trim(),
            category = state.category.trim(),
            image = state.imageOrDefault
        )
        // Deshabilita el botón mientras se muestra el mensaje de éxito
        _uiState.update { it.copy(isLoading = true) }
        onSuccess()
    }
}
