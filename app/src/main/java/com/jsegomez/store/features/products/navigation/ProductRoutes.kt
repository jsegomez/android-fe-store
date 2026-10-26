package com.jsegomez.store.features.products.navigation

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable data object Products
@Serializable data object ProductList
@Serializable data object ProductRegister

fun NavController.navigateToProductRegister() {
    navigate(ProductRegister) { launchSingleTop = true }
}

fun NavController.navigateToProductList() {
    navigate(ProductList) { launchSingleTop = true }
}

fun NavController.navigateToProductListAfterSave() {
    navigate(ProductList) {
        // Reemplaza el registro por la lista: "atrás" regresa a Home
        popUpTo(ProductRegister) { inclusive = true }
        launchSingleTop = true
    }
}
