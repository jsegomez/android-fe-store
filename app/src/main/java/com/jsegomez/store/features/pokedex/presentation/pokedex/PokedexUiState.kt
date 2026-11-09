package com.jsegomez.store.features.pokedex.presentation.pokedex

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

// Cada campo responde a una pregunta distinta. Por eso un fallo de red (refreshError)
// NO reemplaza a la lista: puede haber datos guardados que siguen siendo útiles.
data class PokedexUiState(
    // Lo que hay guardado en el teléfono (viene de Room)
    val pokemon: List<PokemonSummary> = emptyList(),
    // true hasta que Room entrega su primera lectura (aunque sea una lista vacía)
    val isLoading: Boolean = true,
    // true mientras hay una descarga de PokéAPI en curso
    val isRefreshing: Boolean = false,
    // La última descarga falló. Puede coexistir con datos guardados.
    val refreshError: AppError? = null,
    // Falló la lectura del almacenamiento local (poco frecuente)
    val error: AppError? = null,
    // true = el filtro "Solo favoritos" está activo y `pokemon` contiene solo los marcados.
    // Vacío con el filtro activo significa "ningún favorito", no "la lista no se ha descargado".
    val onlyFavorites: Boolean = false,
)
