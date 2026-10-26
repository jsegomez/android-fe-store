package com.jsegomez.store.features.products.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.jsegomez.store.features.products.presentation.list.ProductListScreen
import com.jsegomez.store.features.products.presentation.register.ProductRegisterScreen

fun NavGraphBuilder.productsGraph(
    onProductSaved: () -> Unit,
    onBack: () -> Unit
) {
    navigation<Products>(startDestination = ProductList) {
        composable<ProductList> {
            ProductListScreen(onBack = onBack)
        }
        composable<ProductRegister> {
            ProductRegisterScreen(
                onProductSaved = onProductSaved,
                onBack = onBack
            )
        }
    }
}
