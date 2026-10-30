package com.jsegomez.store.features.pokedex.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.usecase.ObserveIsFavoriteUseCase
import com.jsegomez.store.features.pokedex.domain.usecase.ToggleFavoriteUseCase
import com.jsegomez.store.features.pokedex.navigation.PokemonDetailRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PokedexRepository,
    observeIsFavorite: ObserveIsFavoriteUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {
    private val pokemonId: Int = savedStateHandle.toRoute<PokemonDetailRoute>().id

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = observeIsFavorite(pokemonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        load()
    }

    fun onToggleFavorite() {
        toggleFavorite(pokemonId)
    }

    fun load() {
        if (_uiState.value is PokemonDetailUiState.Success) return
        _uiState.value = PokemonDetailUiState.Loading

        viewModelScope.launch {
            repository.getPokemonDetail(pokemonId)
                .onSuccess { _uiState.value = PokemonDetailUiState.Success(it) }
                .onFailure { _uiState.value = PokemonDetailUiState.Error(it as? AppError ?: AppError.Unknown(it)) }
        }
    }
}
