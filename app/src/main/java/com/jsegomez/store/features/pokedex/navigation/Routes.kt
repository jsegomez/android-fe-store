package com.jsegomez.store.features.pokedex.navigation

import kotlinx.serialization.Serializable

@Serializable data object PokedexRoute

@Serializable data class PokemonDetailRoute(val id: Int)