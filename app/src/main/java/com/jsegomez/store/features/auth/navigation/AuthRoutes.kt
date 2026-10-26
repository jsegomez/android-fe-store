package com.jsegomez.store.features.auth.navigation

import androidx.navigation.NavController
import com.jsegomez.store.navigation.Auth
import com.jsegomez.store.navigation.Main
import kotlinx.serialization.Serializable

@Serializable data object Login
@Serializable data object Register

fun NavController.navigateToMainAfterLogin() {
    navigate(Main) {
        // Saca todo el subgrafo Auth del back stack para que "atrás" no regrese al login
        popUpTo(Auth) { inclusive = true }
    }
}
