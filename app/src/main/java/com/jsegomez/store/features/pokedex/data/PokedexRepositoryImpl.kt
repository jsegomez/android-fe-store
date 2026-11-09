package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.core.network.safeApiCall
import com.jsegomez.store.features.pokedex.data.local.PokemonDao
import com.jsegomez.store.features.pokedex.data.local.PokemonDetailDao
import com.jsegomez.store.features.pokedex.data.mapper.toDetailEntity
import com.jsegomez.store.features.pokedex.data.mapper.toDomain
import com.jsegomez.store.features.pokedex.data.mapper.toEntity
import com.jsegomez.store.features.pokedex.data.mapper.toStatEntities
import com.jsegomez.store.features.pokedex.data.mapper.toSummaryEntity
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PokedexRepositoryImpl(
    private val api: PokeApi,
    private val pokemonDao: PokemonDao,
    private val detailDao: PokemonDetailDao,
) : PokedexRepository {

    // Lo que ve el usuario sale SIEMPRE de Room, haya internet o no.
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

    override fun observePokemonDetail(id: Int): Flow<PokemonDetail?> =
        detailDao.observe(id).map { cached -> cached?.toDomain() }

    // Misma idea que la lista: la red va primero y la escritura solo ocurre si la descarga salió bien.
    // saveDetail guarda resumen, ficha y estadísticas en UNA transacción: nunca queda
    // una ficha a medias, y si algo falla se conserva la anterior.
    // La fecha se toma DESPUÉS de recibir la respuesta: así solo queda registrada una descarga
    // que de verdad ocurrió, y un fallo de red nunca "rejuvenece" una ficha antigua.
    override suspend fun refreshPokemonDetail(id: Int): Result<Unit> =
        safeApiCall {
            val dto = api.getPokemonDetail(id.toString())
            detailDao.saveDetail(
                summary = dto.toSummaryEntity(),
                detail = dto.toDetailEntity(lastUpdated = System.currentTimeMillis()),
                stats = dto.toStatEntities(),
            )
        }
}
