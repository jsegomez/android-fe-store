package com.jsegomez.store.features.auth.presentation.login

import android.util.Patterns

data class LoginUiState(
    val user: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
) {
    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(user).matches()

    val isPasswordValid: Boolean
        get() = password.length >= 6

    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid
}
