package com.jsegomez.store.features.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jsegomez.store.features.home.HomeScreen

fun NavGraphBuilder.homeGraph(
    onNavigateToProductRegister: () -> Unit,
    onNavigateToProductList: () -> Unit,
    onNavigateToPokedex: () -> Unit
) {
    composable<Home> {
        HomeScreen(
            onNavigateToProductRegister = onNavigateToProductRegister,
            onNavigateToProductList = onNavigateToProductList,
            onNavigateToPokedex = onNavigateToPokedex
        )
    }
}
