package com.jsegomez.store.features.pokedex.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokedexViewModel(
    private val repository: PokedexRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    init {
        loadPokemon()
    }

    fun loadPokemon() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.getPokemonList(limit = PAGE_SIZE, offset = 0)
                .onSuccess { list ->
                    _uiState.update { it.copy(pokemon = list, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e as? AppError ?: AppError.Unknown(e))
                    }
                }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }

}