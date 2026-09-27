package com.jsegomez.store.features.products.presentation.register

data class ProductRegisterUiState(
    val name: String = "",
    val price: String = "",
    val stock: String = "",
    val isLoading: Boolean = false,
) {
    val isNameValid: Boolean
        get() = name.isNotBlank()

    val isPriceValid: Boolean
        get() = price.toDoubleOrNull()?.let { it > 0 } == true

    val isStockValid: Boolean
        get() = stock.toIntOrNull()?.let { it >= 0 } == true

    val isFormValid: Boolean
        get() = isNameValid && isPriceValid && isStockValid
}
