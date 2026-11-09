package com.jsegomez.store.features.pokedex.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokedexViewModel(
    private val repository: PokedexRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        // Dos tareas independientes: mostrar lo guardado ya, y pedir datos nuevos a la red.
        observePokemon()
        refreshPokemon()
    }

    // Botón "Reintentar" de la pantalla. Solo reinicia la lectura local si esa lectura había
    // fallado; en cualquier caso vuelve a pedir datos a la red.
    fun retry() {
        if (_uiState.value.error != null) observePokemon()
        refreshPokemon()
    }

    // Se queda escuchando a Room: cada vez que cambia la tabla llega la lista nueva.
    // Es la ÚNICA fuente de la lista que ve la pantalla (la descarga no la asigna directamente).
    private fun observePokemon() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observePokemonList()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.toAppError()) }
                }
                .collect { list ->
                    _uiState.update { it.copy(pokemon = list, isLoading = false, error = null) }
                }
        }
    }

    private fun refreshPokemon() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, refreshError = null) }

        viewModelScope.launch {
            // Si falla, solo se registra el error: NO se toca state.pokemon, así la lista guardada
            // sigue en pantalla. Si funciona, Room avisa a observePokemon() y la lista se actualiza sola.
            repository.refreshPokemonList(limit = PAGE_SIZE, offset = 0)
                .onFailure { e ->
                    _uiState.update { it.copy(refreshError = e.toAppError()) }
                }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun Throwable.toAppError(): AppError = this as? AppError ?: AppError.Unknown(this)

    private companion object {
        const val PAGE_SIZE = 20
    }
}
