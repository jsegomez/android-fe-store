package com.jsegomez.store.features.pokedex.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.jsegomez.store.features.pokedex.navigation.PokemonDetailRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val pokemonId: Int = savedStateHandle.toRoute<PokemonDetailRoute>().id

    private val _uiState = MutableStateFlow(PokemonDetailsUiState(pokemonId = pokemonId))
    val uiState: StateFlow<PokemonDetailsUiState> = _uiState.asStateFlow()
}