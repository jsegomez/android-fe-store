package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.core.network.safeApiCall
import com.jsegomez.store.features.pokedex.data.mapper.toDomain
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

class PokedexRepositoryImpl(private val api: PokeApi) : PokedexRepository {
    override suspend fun getPokemonList(limit: Int, offset: Int): Result<List<PokemonSummary>> =
        safeApiCall { api.getPokemonList(limit, offset) }
            .map { it.toDomain() }

    override suspend fun getPokemonDetail(id: Int): Result<PokemonDetail> =
        safeApiCall { api.getPokemonDetail(id.toString()) }
            .map { it.toDomain() }
}