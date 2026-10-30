package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.features.pokedex.domain.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Los favoritos viven solo en memoria: se pierden al cerrar la app
class InMemoryFavoriteRepository : FavoriteRepository {
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    override val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    override fun toggle(pokemonId: Int) {
        _favoriteIds.update { current ->
            if (pokemonId in current) current - pokemonId else current + pokemonId
        }
    }
}
