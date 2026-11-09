package com.jsegomez.store.features.pokedex.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Base real de Room en MEMORIA. Aquí se prueba la lógica de los favoritos y, sobre todo, que están
// aislados de la caché. La supervivencia a un reinicio se prueba en FavoritePersistenceTest.
@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {
    private lateinit var database: PokedexDatabase
    private lateinit var pokemonDao: PokemonDao
    private lateinit var detailDao: PokemonDetailDao
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokedexDatabase::class.java).build()
        pokemonDao = database.pokemonDao()
        detailDao = database.detailDao()
        favoriteDao = database.favoriteDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun pokemon_isNotFavorite_byDefault() = runBlocking {
        assertFalse(favoriteDao.observeIsFavorite(1).first())
    }

    @Test
    fun toggle_marksThePokemon_andTogglingAgainUnmarksIt() = runBlocking {
        favoriteDao.toggle(1)
        assertTrue(favoriteDao.observeIsFavorite(1).first())

        favoriteDao.toggle(1)
        assertFalse(favoriteDao.observeIsFavorite(1).first())
    }

    @Test
    fun toggle_doesNotAffectOtherPokemon() = runBlocking {
        favoriteDao.toggle(1)

        assertFalse(favoriteDao.observeIsFavorite(2).first())
    }

    @Test
    fun observeFavoritePokemon_returnsOnlyTheMarkedSummaries_orderedById() = runBlocking {
        pokemonDao.upsertAll(listOf(summary(25), summary(1), summary(4)))
        // Se marcan en orden distinto al de los ids: el orden debe venir de la consulta
        favoriteDao.toggle(25)
        favoriteDao.toggle(1)

        val favorites = favoriteDao.observeFavoritePokemon().first()

        assertEquals(listOf(1, 25), favorites.map { it.id })
    }

    @Test
    fun observeFavoritePokemon_omitsAFavoriteWhoseSummaryIsNotCached_untilItArrives() = runBlocking {
        // Caso límite documentado en FavoriteEntity: favorito sin resumen en la caché
        favoriteDao.toggle(7)
        assertTrue(favoriteDao.observeFavoritePokemon().first().isEmpty())
        assertTrue(favoriteDao.observeIsFavorite(7).first()) // sigue marcado

        pokemonDao.upsertAll(listOf(summary(7)))

        assertEquals(listOf(7), favoriteDao.observeFavoritePokemon().first().map { it.id })
    }

    // ---------- Los favoritos son independientes de la caché ----------

    @Test
    fun favorites_surviveARefreshOfTheList() = runBlocking {
        pokemonDao.upsertAll(listOf(summary(1), summary(2)))
        favoriteDao.toggle(1)

        // Un refresh de la lista vuelve a escribir los mismos resúmenes (con datos nuevos)
        pokemonDao.upsertAll(listOf(summary(1, name = "bulbasaur-nuevo"), summary(2)))

        assertTrue(favoriteDao.observeIsFavorite(1).first())
        assertEquals("bulbasaur-nuevo", favoriteDao.observeFavoritePokemon().first().single().name)
    }

    @Test
    fun favorites_surviveARefreshOfTheDetail() = runBlocking {
        detailDao.saveDetail(summary(1), detail(1), emptyList())
        favoriteDao.toggle(1)

        // Un refresh de la ficha reemplaza resumen, ficha y estadísticas
        detailDao.saveDetail(summary(1), detail(1), listOf(PokemonStatEntity(1, "hp", 45, 0)))

        assertTrue(favoriteDao.observeIsFavorite(1).first())
    }

    @Test
    fun favorites_surviveWipingTheWholeCache() = runBlocking {
        detailDao.saveDetail(summary(1), detail(1), listOf(PokemonStatEntity(1, "hp", 45, 0)))
        favoriteDao.toggle(1)

        // Se borra TODA la caché, de la forma más agresiva posible. Con una clave foránea en CASCADE
        // esto se llevaría el favorito; sin ella, el favorito del usuario queda intacto.
        val db = database.openHelper.writableDatabase
        db.execSQL("DELETE FROM pokemon_stats")
        db.execSQL("DELETE FROM pokemon_details")
        db.execSQL("DELETE FROM pokemon")

        assertTrue(favoriteDao.observeIsFavorite(1).first())
    }

    private fun summary(id: Int, name: String = "pokemon-$id") =
        PokemonEntity(id = id, name = name, imageUrl = "https://example.com/$id.png")

    private fun detail(id: Int) = PokemonDetailEntity(
        pokemonId = id,
        heightDm = 7,
        weightHg = 69,
        typesJson = """["grass"]""",
        abilitiesJson = """[]""",
        lastUpdated = 1_000L,
    )
}
