package com.jsegomez.store.features.pokedex.domain

import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

interface PokedexRepository {

    // LEER y ACTUALIZAR son dos operaciones separadas, tanto para la lista como para el detalle:
    // - observe: lo que hay guardado en el teléfono. No toca la red y emite de nuevo cuando cambia.
    // - refresh: descarga de PokéAPI y guarda. No devuelve datos: la pantalla los recibe por observe.
    fun observePokemonList(): Flow<List<PokemonSummary>>

    suspend fun refreshPokemonList(limit: Int, offset: Int): Result<Unit>

    // Emite null mientras la ficha de ese Pokémon NO esté descargada (aunque aparezca en la lista)
    fun observePokemonDetail(id: Int): Flow<PokemonDetail?>

    suspend fun refreshPokemonDetail(id: Int): Result<Unit>
}
