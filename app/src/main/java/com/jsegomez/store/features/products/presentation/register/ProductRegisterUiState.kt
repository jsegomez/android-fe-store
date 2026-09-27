package com.jsegomez.store.features.products.presentation.register

data class ProductRegisterUiState(
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val category: String = "",
    val image: String = "",
    val isLoading: Boolean = false,
) {
    val isTitleValid: Boolean
        get() = title.isNotBlank()

    val isPriceValid: Boolean
        get() = price.toDoubleOrNull()?.let { it > 0 } == true

    val isDescriptionValid: Boolean
        get() = description.isNotBlank()

    val isCategoryValid: Boolean
        get() = category.isNotBlank()

    val isImageValid: Boolean
        get() = image.startsWith("http://") || image.startsWith("https://")

    val isFormValid: Boolean
        get() = isTitleValid && isPriceValid && isDescriptionValid && isCategoryValid && isImageValid
}
