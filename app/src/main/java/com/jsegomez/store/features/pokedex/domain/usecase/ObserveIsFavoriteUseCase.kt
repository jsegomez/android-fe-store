package com.jsegomez.store.features.pokedex.domain.usecase

import com.jsegomez.store.features.pokedex.domain.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveIsFavoriteUseCase(private val repository: FavoriteRepository) {
    operator fun invoke(pokemonId: Int): Flow<Boolean> =
        repository.favoriteIds.map { pokemonId in it }
}
