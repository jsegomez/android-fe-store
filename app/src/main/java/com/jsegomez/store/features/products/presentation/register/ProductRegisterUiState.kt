package com.jsegomez.store.features.products.presentation.register

private const val MIN_TEXT_LENGTH = 3
// Se pide en PNG porque placehold.co devuelve SVG por defecto y Coil no lo decodifica
private const val DEFAULT_IMAGE = "https://placehold.co/600x600.png?text=Sin+imagen"

data class ProductRegisterUiState(
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val category: String = "",
    val image: String = "",
    val isLoading: Boolean = false,
) {
    val isTitleValid: Boolean
        get() = title.trim().length >= MIN_TEXT_LENGTH

    val isPriceValid: Boolean
        get() = price.toDoubleOrNull()?.let { it > 0 } == true

    val isDescriptionValid: Boolean
        get() = description.trim().length >= MIN_TEXT_LENGTH

    val isCategoryValid: Boolean
        get() = category.trim().length >= MIN_TEXT_LENGTH

    // La imagen es opcional: vacía es válida, y si se escribe debe ser una URL
    val isImageValid: Boolean
        get() = image.isBlank() || image.startsWith("http://") || image.startsWith("https://")

    // Imagen que se guardará: la escrita o, si no hay ninguna, la de por defecto
    val imageOrDefault: String
        get() = image.trim().ifEmpty { DEFAULT_IMAGE }

    val isFormValid: Boolean
        get() = isTitleValid && isPriceValid && isDescriptionValid && isCategoryValid && isImageValid
}
