package com.jsegomez.store.features.auth.presentation

data class LoginUiState(
    val user: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)
