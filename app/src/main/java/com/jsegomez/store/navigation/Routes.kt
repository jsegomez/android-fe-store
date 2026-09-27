package com.jsegomez.store.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object Home : Route
    @Serializable data object HomeProducts : Route

}
