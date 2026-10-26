package com.jsegomez.store.features.pokedex.navigation

import androidx.navigation.NavController
import com.jsegomez.store.navigation.Pokedex
import kotlinx.serialization.Serializable

@Serializable data object PokedexRoute

@Serializable data class PokemonDetailRoute(val id: Int)

fun NavController.navigateToPokedex() {
    navigate(Pokedex) { launchSingleTop = true }
}

fun NavController.navigateToPokemonDetail(id: Int) {
    navigate(PokemonDetailRoute(id)) { launchSingleTop = true }
}