package com.jsegomez.store.features.pokedex.domain

import kotlinx.coroutines.flow.StateFlow

interface FavoriteRepository {
    val favoriteIds: StateFlow<Set<Int>>

    // Si el Pokémon ya es favorito lo desmarca; si no, lo marca
    fun toggle(pokemonId: Int)
}
