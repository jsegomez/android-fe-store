package com.jsegomez.store.features.pokedex.domain.usecase

import com.jsegomez.store.features.pokedex.domain.FavoriteRepository

class ToggleFavoriteUseCase(private val repository: FavoriteRepository) {
    operator fun invoke(pokemonId: Int) = repository.toggle(pokemonId)
}
