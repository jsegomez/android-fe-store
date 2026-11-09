package com.jsegomez.store.features.pokedex.domain

import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    // ¿Este Pokémon es favorito? Emite de nuevo cada vez que cambia
    fun observeIsFavorite(pokemonId: Int): Flow<Boolean>

    // Los Pokémon marcados, en el mismo formato que la lista, para el filtro "Solo favoritos"
    fun observeFavoritePokemon(): Flow<List<PokemonSummary>>

    // Si el Pokémon ya es favorito lo desmarca; si no, lo marca. Escribe en disco, por eso es suspend.
    suspend fun toggle(pokemonId: Int)
}
