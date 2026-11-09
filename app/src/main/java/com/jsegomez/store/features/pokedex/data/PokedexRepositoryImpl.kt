package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.core.network.safeApiCall
import com.jsegomez.store.features.pokedex.data.local.PokemonDao
import com.jsegomez.store.features.pokedex.data.mapper.toDomain
import com.jsegomez.store.features.pokedex.data.mapper.toEntity
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PokedexRepositoryImpl(
    private val api: PokeApi,
    private val pokemonDao: PokemonDao,
) : PokedexRepository {

    // La lista que ve el usuario sale SIEMPRE de Room, haya internet o no.
    override fun observePokemonList(): Flow<List<PokemonSummary>> =
        pokemonDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    // Orden de las operaciones: primero descargar y convertir, y solo si todo salió bien, guardar.
    // Si la red falla, nunca se llega a escribir y la última copia válida queda intacta.
    // safeApiCall traduce cualquier excepción del bloque a AppError; aquí también cubre
    // un fallo al escribir en disco, no solo los de red.
    override suspend fun refreshPokemonList(limit: Int, offset: Int): Result<Unit> =
        safeApiCall {
            val summaries = api.getPokemonList(limit, offset).toDomain()
            pokemonDao.upsertAll(summaries.map { it.toEntity() })
        }

    override suspend fun getPokemonDetail(id: Int): Result<PokemonDetail> =
        safeApiCall { api.getPokemonDetail(id.toString()) }
            .map { it.toDomain() }
}
