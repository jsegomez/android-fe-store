package com.jsegomez.store.features.pokedex.domain

import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

interface PokedexRepository {
    suspend fun getPokemonList(limit: Int, offset: Int): Result<List<PokemonSummary>>
    suspend fun getPokemonDetail(id: Int): Result<PokemonDetail>
}