package com.jsegomez.store.features.pokedex.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsegomez.store.core.network.toAppError
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.usecase.ObserveFavoritePokemonUseCase
import com.jsegomez.store.features.pokedex.domain.usecase.RefreshPokemonListUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokedexViewModel(
    private val repository: PokedexRepository,
    private val observeFavoritePokemon: ObserveFavoritePokemonUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    // Estado del filtro "Solo favoritos". Es la entrada de observePokemon(): al cambiarlo, la
    // lectura pasa a otra fuente. Se copia también a uiState para que la pantalla pueda mostrarlo.
    private val onlyFavorites = MutableStateFlow(false)

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

    // Activa o desactiva "Solo favoritos". Solo cambia QUÉ se lee de Room: no vuelve a descargar nada,
    // así que funciona igual sin internet.
    fun onToggleOnlyFavorites() {
        val enabled = !onlyFavorites.value
        onlyFavorites.value = enabled
        _uiState.update { it.copy(onlyFavorites = enabled) }
    }

    // Se queda escuchando a Room: cada vez que cambia la tabla llega la lista nueva.
    // Es la ÚNICA fuente de la lista que ve la pantalla (la descarga no la asigna directamente).
    // flatMapLatest: cada vez que cambia el filtro se descarta la lectura anterior y se empieza
    // a leer la fuente que corresponde (todos los Pokémon o solo los favoritos).
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePokemon() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            onlyFavorites
                .flatMapLatest { only ->
                    if (only) observeFavoritePokemon() else repository.observePokemonList()
                }
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
            repository.refreshPokemonList(limit = RefreshPokemonListUseCase.PAGE_SIZE, offset = 0)
                .onFailure { e ->
                    _uiState.update { it.copy(refreshError = e.toAppError()) }
                }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
