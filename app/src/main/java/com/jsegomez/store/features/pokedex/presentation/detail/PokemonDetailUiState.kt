package com.jsegomez.store.features.pokedex.presentation.detail

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail

sealed interface PokemonDetailUiState {
    data object Loading : PokemonDetailUiState
    data class Success(val pokemon: PokemonDetail) : PokemonDetailUiState
    data class Error(val error: AppError) : PokemonDetailUiState
}
