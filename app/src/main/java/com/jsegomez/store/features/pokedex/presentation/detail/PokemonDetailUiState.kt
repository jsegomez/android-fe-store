package com.jsegomez.store.features.pokedex.presentation.detail

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail

// Misma idea que PokedexUiState: cada campo responde a una pregunta distinta, y un fallo de red
// (refreshError) NO reemplaza a la ficha cuando ya la tenemos guardada.
data class PokemonDetailUiState(
    // La ficha guardada en el teléfono. null = esta ficha todavía no se ha descargado
    // (aunque el Pokémon aparezca en la lista, que solo trae id y nombre).
    val pokemon: PokemonDetail? = null,
    // true hasta que Room entrega su primera lectura (aunque no haya ficha)
    val isLoading: Boolean = true,
    // true mientras hay una descarga de PokéAPI en curso
    val isRefreshing: Boolean = false,
    // La última descarga falló. Puede coexistir con una ficha guardada.
    val refreshError: AppError? = null,
    // Falló la lectura del almacenamiento local (poco frecuente)
    val error: AppError? = null,
)
