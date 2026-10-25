package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.core.network.safeApiCall
import com.jsegomez.store.di.NetworkModule
import com.jsegomez.store.features.pokedex.data.mapper.toDomain
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary

class PokedexRepositoryImpl(private val api: PokeApi = NetworkModule.pokeApi) : PokedexRepository {
    override suspend fun getPokemonList(limit: Int, offset: Int): Result<List<PokemonSummary>> =
        safeApiCall { api.getPokemonList(limit, offset) }
            .map { it.toDomain() }
}