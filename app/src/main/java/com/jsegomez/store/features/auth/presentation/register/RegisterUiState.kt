package com.jsegomez.store.features.auth.presentation.register

import android.util.Patterns

data class RegisterUiState(
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
){
    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isNameValid: Boolean
        get() = name.length >= 3
    val isLastNameValid: Boolean
        get() = lastName.length >= 3
    val isFormValid: Boolean
        get() = isEmailValid && isNameValid && isLastNameValid
}
