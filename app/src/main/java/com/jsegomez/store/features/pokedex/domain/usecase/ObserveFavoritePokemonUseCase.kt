package com.jsegomez.store.features.pokedex.domain.usecase

import com.jsegomez.store.features.pokedex.domain.FavoriteRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

class ObserveFavoritePokemonUseCase(private val repository: FavoriteRepository) {
    operator fun invoke(): Flow<List<PokemonSummary>> = repository.observeFavoritePokemon()
}
