package com.jsegomez.store.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jsegomez.store.features.auth.presentation.LoginScreen
import com.jsegomez.store.features.home.HomeScreen
import com.jsegomez.store.features.products.presentation.list.ProductListScreen
import com.jsegomez.store.features.products.presentation.register.ProductRegisterScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Login,
        modifier = modifier
    ) {
        composable<Route.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home) {
                        // Saca Login del back stack para que "atrás" no regrese al login
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }
        composable<Route.Home> { HomeScreen() }
        composable<Route.ProductList> { ProductListScreen() }
        composable<Route.ProductRegister> { ProductRegisterScreen() }
    }
}


