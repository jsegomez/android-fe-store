package com.jsegomez.store.features.pokedex.domain

import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

interface PokedexRepository {

    // LEER y ACTUALIZAR son dos operaciones separadas:
    // - observe: lo que hay guardado en el teléfono. No toca la red y emite de nuevo cuando cambia.
    // - refresh: descarga de PokéAPI y guarda. No devuelve la lista: la pantalla la recibe por observe.
    fun observePokemonList(): Flow<List<PokemonSummary>>

    suspend fun refreshPokemonList(limit: Int, offset: Int): Result<Unit>

    // El detalle sigue yendo directo a la red hasta la Fase B
    suspend fun getPokemonDetail(id: Int): Result<PokemonDetail>
}
