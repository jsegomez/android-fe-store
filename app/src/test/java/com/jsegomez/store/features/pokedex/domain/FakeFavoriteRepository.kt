package com.jsegomez.store.features.pokedex.domain

import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// Repositorio de favoritos FALSO, en memoria, para pruebas de JVM (casos de uso y ViewModels).
// El de verdad guarda en Room y se prueba aparte, en androidTest.
// `catalog` simula los resúmenes que hay en la caché: el filtro solo devuelve los marcados que estén ahí.
class FakeFavoriteRepository(
    private val catalog: List<PokemonSummary> = emptyList(),
) : FavoriteRepository {
    private val favoriteIds = MutableStateFlow<Set<Int>>(emptySet())

    override fun observeIsFavorite(pokemonId: Int): Flow<Boolean> =
        favoriteIds.map { pokemonId in it }

    override fun observeFavoritePokemon(): Flow<List<PokemonSummary>> =
        favoriteIds.map { ids -> catalog.filter { it.id in ids } }

    override suspend fun toggle(pokemonId: Int) {
        favoriteIds.update { current ->
            if (pokemonId in current) current - pokemonId else current + pokemonId
        }
    }
}
