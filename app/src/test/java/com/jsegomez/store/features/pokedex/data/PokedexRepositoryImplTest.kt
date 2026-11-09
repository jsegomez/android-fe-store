package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.data.local.PokemonDao
import com.jsegomez.store.features.pokedex.data.local.PokemonEntity
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonDetailDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonListDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonSummaryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

// Prueba del repositorio con una API y un DAO FALSOS (los de abajo): no usa red ni base real,
// solo comprueba la lógica del repositorio: qué guarda, en qué orden y qué hace cuando algo falla.
class PokedexRepositoryImplTest {
    private val api = FakePokeApi()
    private val dao = FakePokemonDao()
    private val repository = PokedexRepositoryImpl(api, dao)

    @Test
    fun observePokemonList_isEmpty_beforeAnyRefresh() = runBlocking {
        assertTrue(repository.observePokemonList().first().isEmpty())
    }

    @Test
    fun refreshPokemonList_savesDownloadedPokemon_andObserveEmitsThem() = runBlocking {
        api.listResponse = listResponse("bulbasaur" to 1, "ivysaur" to 2)

        val result = repository.refreshPokemonList(limit = 20, offset = 0)

        assertTrue(result.isSuccess)
        val saved = repository.observePokemonList().first()
        assertEquals(listOf(1 to "bulbasaur", 2 to "ivysaur"), saved.map { it.id to it.name })
    }

    @Test
    fun refreshPokemonList_failure_returnsErrorAndKeepsSavedPokemon() = runBlocking {
        api.listResponse = listResponse("bulbasaur" to 1, "ivysaur" to 2)
        repository.refreshPokemonList(limit = 20, offset = 0)

        // Segundo refresh sin internet: la copia anterior no debe tocarse
        api.failure = UnknownHostException()
        val result = repository.refreshPokemonList(limit = 20, offset = 0)

        assertTrue(result.exceptionOrNull() is AppError.NoConnection)
        assertEquals(listOf(1, 2), repository.observePokemonList().first().map { it.id })
    }

    @Test
    fun refreshPokemonList_sameDataTwice_doesNotDuplicateRows() = runBlocking {
        api.listResponse = listResponse("bulbasaur" to 1)

        repository.refreshPokemonList(limit = 20, offset = 0)
        repository.refreshPokemonList(limit = 20, offset = 0)

        assertEquals(1, repository.observePokemonList().first().size)
    }

    private fun listResponse(vararg pokemon: Pair<String, Int>) = PokemonListDto(
        count = pokemon.size.toLong(),
        next = null,
        previous = null,
        results = pokemon.map { (name, id) ->
            PokemonSummaryDto(name = name, url = "https://pokeapi.co/api/v2/pokemon/$id/")
        },
    )
}

// API falsa: devuelve lo que el test configure, o lanza el error indicado
private class FakePokeApi : PokeApi {
    var listResponse: PokemonListDto? = null
    var failure: Exception? = null

    override suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto {
        failure?.let { throw it }
        return checkNotNull(listResponse) { "El test no configuró listResponse" }
    }

    override suspend fun getPokemonDetail(idOrName: String): PokemonDetailDto =
        error("El detalle no se usa en estas pruebas")
}

// DAO falso en memoria: replica lo esencial del real (Upsert por id y orden por id)
private class FakePokemonDao : PokemonDao {
    private val rows = MutableStateFlow<Map<Int, PokemonEntity>>(emptyMap())

    override fun observeAll(): Flow<List<PokemonEntity>> =
        rows.map { it.values.sortedBy { row -> row.id } }

    override suspend fun upsertAll(pokemon: List<PokemonEntity>) {
        rows.value = rows.value + pokemon.associateBy { it.id }
    }
}
