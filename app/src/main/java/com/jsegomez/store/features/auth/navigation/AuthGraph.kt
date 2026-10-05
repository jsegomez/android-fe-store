package com.jsegomez.store.features.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.jsegomez.store.features.auth.presentation.login.LoginScreen
import com.jsegomez.store.features.auth.presentation.register.RegisterScreen
import com.jsegomez.store.navigation.Auth

// Subgrafo de autenticación: agrupa todas las pantallas del flujo de auth.
fun NavGraphBuilder.authGraph(onLoginSuccess: () -> Unit) {
    navigation<Auth>(startDestination = Login) {
        composable<Login> { LoginScreen(onLoginSuccess = onLoginSuccess) }
        composable<Register> { RegisterScreen() }
    }
}
