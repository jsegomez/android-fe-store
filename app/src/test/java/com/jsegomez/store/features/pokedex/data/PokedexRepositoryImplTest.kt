package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.data.local.CachedPokemonDetail
import com.jsegomez.store.features.pokedex.data.local.PokemonDao
import com.jsegomez.store.features.pokedex.data.local.PokemonDetailDao
import com.jsegomez.store.features.pokedex.data.local.PokemonDetailEntity
import com.jsegomez.store.features.pokedex.data.local.PokemonEntity
import com.jsegomez.store.features.pokedex.data.local.PokemonStatEntity
import com.jsegomez.store.features.pokedex.data.remote.PokeApi
import com.jsegomez.store.features.pokedex.data.remote.dto.AbilitySlotDto
import com.jsegomez.store.features.pokedex.data.remote.dto.NamedResourceDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonDetailDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonListDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonSummaryDto
import com.jsegomez.store.features.pokedex.data.remote.dto.SpritesDto
import com.jsegomez.store.features.pokedex.data.remote.dto.StatSlotDto
import com.jsegomez.store.features.pokedex.data.remote.dto.TypeSlotDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

// Prueba del repositorio con una API y unos DAO FALSOS (los de abajo): no usa red ni base real,
// solo comprueba la lógica del repositorio: qué guarda, en qué orden y qué hace cuando algo falla.
// Que los DAO reales funcionen se prueba aparte, en androidTest.
class PokedexRepositoryImplTest {
    private val api = FakePokeApi()
    private val repository = PokedexRepositoryImpl(api, FakePokemonDao(), FakePokemonDetailDao())

    // ---------- Lista ----------

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

    // ---------- Detalle ----------

    @Test
    fun observePokemonDetail_isNull_beforeAnyRefresh() = runBlocking {
        assertNull(repository.observePokemonDetail(1).first())
    }

    @Test
    fun refreshPokemonDetail_savesTheDetail_andObserveEmitsItConverted() = runBlocking {
        api.detailResponse = bulbasaurDetail()

        val result = repository.refreshPokemonDetail(id = 1)

        assertTrue(result.isSuccess)
        val detail = repository.observePokemonDetail(1).first()
        assertNotNull(detail)
        assertEquals("bulbasaur", detail!!.name)
        // La API entrega decímetros y hectogramos; el dominio trabaja en metros y kilogramos
        assertEquals(0.7, detail.heightMeters, 0.0001)
        assertEquals(6.9, detail.weightKg, 0.0001)
        assertEquals(listOf("hp", "attack"), detail.stats.map { it.name })
    }

    @Test
    fun refreshPokemonDetail_failure_returnsErrorAndKeepsTheSavedDetail() = runBlocking {
        api.detailResponse = bulbasaurDetail()
        repository.refreshPokemonDetail(id = 1)

        api.failure = UnknownHostException()
        val result = repository.refreshPokemonDetail(id = 1)

        assertTrue(result.exceptionOrNull() is AppError.NoConnection)
        assertEquals("bulbasaur", repository.observePokemonDetail(1).first()?.name)
    }

    @Test
    fun refreshPokemonDetail_failure_withNothingSaved_leavesTheDetailAbsent() = runBlocking {
        api.failure = UnknownHostException()

        val result = repository.refreshPokemonDetail(id = 1)

        assertTrue(result.isFailure)
        assertNull(repository.observePokemonDetail(1).first())
    }

    // ---------- Datos de ejemplo ----------

    private fun listResponse(vararg pokemon: Pair<String, Int>) = PokemonListDto(
        count = pokemon.size.toLong(),
        next = null,
        previous = null,
        results = pokemon.map { (name, id) ->
            PokemonSummaryDto(name = name, url = "https://pokeapi.co/api/v2/pokemon/$id/")
        },
    )

    private fun bulbasaurDetail() = PokemonDetailDto(
        id = 1,
        name = "bulbasaur",
        height = 7,
        weight = 69,
        baseExperience = 64,
        types = listOf(TypeSlotDto(slot = 1, type = named("grass"))),
        abilities = listOf(AbilitySlotDto(ability = named("overgrow"), isHidden = false, slot = 1)),
        stats = listOf(
            StatSlotDto(baseStat = 45, effort = 0, stat = named("hp")),
            StatSlotDto(baseStat = 49, effort = 0, stat = named("attack")),
        ),
        sprites = SpritesDto(),
    )

    private fun named(name: String) = NamedResourceDto(name = name, url = "https://pokeapi.co/api/v2/x/$name/")
}

// API falsa: devuelve lo que el test configure, o lanza el error indicado
private class FakePokeApi : PokeApi {
    var listResponse: PokemonListDto? = null
    var detailResponse: PokemonDetailDto? = null
    var failure: Exception? = null

    override suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto {
        failure?.let { throw it }
        return checkNotNull(listResponse) { "El test no configuró listResponse" }
    }

    override suspend fun getPokemonDetail(idOrName: String): PokemonDetailDto {
        failure?.let { throw it }
        return checkNotNull(detailResponse) { "El test no configuró detailResponse" }
    }
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

// DAO de fichas falso. Guarda la ficha completa en saveDetail y la devuelve en observe.
// El DAO real es una clase abstracta, por eso el fake la hereda y debe declarar también sus
// piezas internas; aquí no se usan, porque el fake guarda todo directamente en saveDetail.
private class FakePokemonDetailDao : PokemonDetailDao() {
    private val saved = MutableStateFlow<Map<Int, CachedPokemonDetail>>(emptyMap())

    override fun observe(pokemonId: Int): Flow<CachedPokemonDetail?> =
        saved.map { it[pokemonId] }

    override suspend fun saveDetail(
        summary: PokemonEntity,
        detail: PokemonDetailEntity,
        stats: List<PokemonStatEntity>,
    ) {
        saved.value = saved.value + (detail.pokemonId to CachedPokemonDetail(detail, summary, stats))
    }

    override suspend fun upsertSummary(summary: PokemonEntity) = unused()
    override suspend fun upsertDetail(detail: PokemonDetailEntity) = unused()
    override suspend fun deleteStats(pokemonId: Int) = unused()
    override suspend fun insertStats(stats: List<PokemonStatEntity>) = unused()

    private fun unused(): Nothing = error("El fake guarda todo en saveDetail")
}
