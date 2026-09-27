package com.jsegomez.store.features.products.presentation.register

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductRegisterViewModel : ViewModel() {
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
        if (!_uiState.value.isFormValid || _uiState.value.isLoading) return
        onSuccess()
    }
}
