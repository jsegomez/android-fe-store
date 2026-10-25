package com.jsegomez.store.features.pokedex.data.remote

import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonDetailDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonListDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApi {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): PokemonListDto

    @GET("pokemon/{idOrName}")
    suspend fun getPokemonDetail(
        @Path("idOrName") idOrName: String,
    ): PokemonDetailDto
}