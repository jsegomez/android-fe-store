package com.jsegomez.store.features.products.presentation.register

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductRegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProductRegisterUiState())
    val uiState: StateFlow<ProductRegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onPriceChange(price: String) {
        _uiState.update { it.copy(price = price) }
    }

    fun onStockChange(stock: String) {
        _uiState.update { it.copy(stock = stock) }
    }

    fun onSaveClick(onSuccess: () -> Unit) {
        if (!_uiState.value.isFormValid || _uiState.value.isLoading) return
        onSuccess()
    }
}
