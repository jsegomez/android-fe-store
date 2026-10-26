package com.jsegomez.store.features.pokedex.presentation.pokedex

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

data class PokedexUiState(
    val pokemon: List<PokemonSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: AppError? = null,

)
