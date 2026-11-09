package com.jsegomez.store.features.pokedex.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jsegomez.store.core.network.toAppError
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.usecase.ObserveIsFavoriteUseCase
import com.jsegomez.store.features.pokedex.domain.usecase.ToggleFavoriteUseCase
import com.jsegomez.store.features.pokedex.navigation.PokemonDetailRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PokedexRepository,
    observeIsFavorite: ObserveIsFavoriteUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {
    private val pokemonId: Int = savedStateHandle.toRoute<PokemonDetailRoute>().id

    private val _uiState = MutableStateFlow(PokemonDetailUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = observeIsFavorite(pokemonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var observeJob: Job? = null

    init {
        // Dos tareas independientes: mostrar lo guardado ya (si hay ficha), y pedir datos nuevos a la red.
        observeDetail()
        refreshDetail()
    }

    // Escribe en disco, así que va en una corrutina. El corazón se actualiza solo: isFavorite
    // observa la tabla y emite de nuevo cuando termina la escritura.
    fun onToggleFavorite() {
        viewModelScope.launch { toggleFavorite(pokemonId) }
    }

    // Botón "Reintentar". Solo reinicia la lectura local si esa lectura había fallado;
    // en cualquier caso vuelve a pedir la ficha a la red.
    fun retry() {
        if (_uiState.value.error != null) observeDetail()
        refreshDetail()
    }

    // Se queda escuchando a Room. Mientras la ficha no esté guardada emite null;
    // cuando termina la descarga, Room avisa y la ficha aparece sola.
    private fun observeDetail() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observePokemonDetail(pokemonId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toAppError()) }
                }
                .collect { detail ->
                    _uiState.update { it.copy(pokemon = detail, isLoading = false, error = null) }
                }
        }
    }

    private fun refreshDetail() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, refreshError = null) }

        viewModelScope.launch {
            // Si falla, solo se registra el error: NO se toca state.pokemon, así una ficha
            // ya guardada sigue en pantalla.
            repository.refreshPokemonDetail(pokemonId)
                .onFailure { e ->
                    _uiState.update { it.copy(refreshError = e.toAppError()) }
                }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
