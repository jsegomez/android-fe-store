package com.jsegomez.store.features.pokedex.navigation

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable data object PokedexRoute

@Serializable data class PokemonDetailRoute(val id: Int)


fun NavController.navigateToPokemonDetail(id: Int) {
    navigate(PokemonDetailRoute(id))
}