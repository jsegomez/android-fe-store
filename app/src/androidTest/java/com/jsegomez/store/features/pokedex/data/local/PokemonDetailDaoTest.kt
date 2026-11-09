package com.jsegomez.store.features.pokedex.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Igual que PokemonDaoTest: base real de Room en MEMORIA, limpia para cada test.
// Aquí se verifican las tres reglas de la ficha: ausente = null, se lee completa con sus
// estadísticas, y se guarda "todo o nada".
@RunWith(AndroidJUnit4::class)
class PokemonDetailDaoTest {
    private lateinit var database: PokedexDatabase
    private lateinit var pokemonDao: PokemonDao
    private lateinit var detailDao: PokemonDetailDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokedexDatabase::class.java).build()
        pokemonDao = database.pokemonDao()
        detailDao = database.detailDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun observe_returnsNull_whenOnlyTheSummaryIsSaved() = runBlocking {
        // El Pokémon está en la lista, pero su ficha nunca se descargó
        pokemonDao.upsertAll(listOf(summary(id = 1)))

        assertNull(detailDao.observe(1).first())
    }

    @Test
    fun saveDetail_thenObserve_returnsTheDetailWithItsSummaryAndStats() = runBlocking {
        detailDao.saveDetail(summary(1), detail(1), stats(1, "hp" to 45, "attack" to 49))

        val cached = detailDao.observe(1).first()

        assertNotNull(cached)
        assertEquals("pokemon-1", cached!!.pokemon.name)
        assertEquals(7, cached.detail.heightDm)
        assertEquals(setOf("hp", "attack"), cached.stats.map { it.name }.toSet())
    }

    @Test
    fun saveDetail_replacesStats_removingTheOnesTheApiNoLongerReturns() = runBlocking {
        detailDao.saveDetail(summary(1), detail(1), stats(1, "hp" to 45, "attack" to 49, "speed" to 45))

        // Segunda descarga: la API ya no trae "speed" y "attack" cambió de valor
        detailDao.saveDetail(summary(1), detail(1), stats(1, "hp" to 45, "attack" to 60))

        val saved = detailDao.observe(1).first()!!.stats.associate { it.name to it.baseValue }
        assertEquals(mapOf("hp" to 45, "attack" to 60), saved)
    }

    @Test
    fun saveDetail_whenAWriteFails_keepsThePreviousDetailUntouched() = runBlocking {
        detailDao.saveDetail(summary(1), detail(1), stats(1, "hp" to 45, "attack" to 49))

        // Estadística de la ficha 999, que no existe: viola la clave foránea justo al insertar,
        // DESPUÉS de que saveDetail ya borró las estadísticas anteriores
        try {
            detailDao.saveDetail(summary(1), detail(1), stats(999, "hp" to 1))
            fail("Se esperaba un error por la clave foránea")
        } catch (expected: Exception) {
            // Lo importante es lo que pasó con los datos, no el tipo exacto de excepción
        }

        // La transacción se deshizo: las estadísticas originales siguen ahí
        val saved = detailDao.observe(1).first()!!.stats.associate { it.name to it.baseValue }
        assertEquals(mapOf("hp" to 45, "attack" to 49), saved)
    }

    @Test
    fun saveDetail_updatesTheSummary_insteadOfDuplicatingIt() = runBlocking {
        detailDao.saveDetail(summary(1, name = "bulbasaur"), detail(1), stats(1, "hp" to 45))
        detailDao.saveDetail(summary(1, name = "bulbasaur-nuevo"), detail(1), stats(1, "hp" to 45))

        val summaries = pokemonDao.observeAll().first()
        assertEquals(1, summaries.size)
        assertEquals("bulbasaur-nuevo", summaries.single().name)
        // Y la ficha sigue ahí: actualizar el resumen no la borró en cascada
        assertNotNull(detailDao.observe(1).first())
    }

    private fun summary(id: Int, name: String = "pokemon-$id") =
        PokemonEntity(id = id, name = name, imageUrl = "https://example.com/$id.png")

    private fun detail(id: Int) = PokemonDetailEntity(
        pokemonId = id,
        heightDm = 7,
        weightHg = 69,
        typesJson = """["grass","poison"]""",
        abilitiesJson = """[{"name":"overgrow","isHidden":false}]""",
    )

    private fun stats(pokemonId: Int, vararg stats: Pair<String, Int>) =
        stats.mapIndexed { index, (name, value) ->
            PokemonStatEntity(pokemonId = pokemonId, name = name, baseValue = value, position = index)
        }
}
