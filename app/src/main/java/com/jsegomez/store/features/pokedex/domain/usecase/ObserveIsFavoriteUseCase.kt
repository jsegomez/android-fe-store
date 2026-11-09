package com.jsegomez.store.features.pokedex.domain.usecase

import com.jsegomez.store.features.pokedex.domain.FavoriteRepository
import kotlinx.coroutines.flow.Flow

class ObserveIsFavoriteUseCase(private val repository: FavoriteRepository) {
    operator fun invoke(pokemonId: Int): Flow<Boolean> = repository.observeIsFavorite(pokemonId)
}
