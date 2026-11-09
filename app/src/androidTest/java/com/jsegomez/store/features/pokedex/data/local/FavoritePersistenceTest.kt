package com.jsegomez.store.features.pokedex.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jsegomez.store.features.pokedex.data.FavoriteRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// PRUEBA DE PERSISTENCIA: a diferencia de las demás, usa una base en ARCHIVO (no en memoria).
// Se escribe, se CIERRA la base y se abre una instancia nueva sobre el mismo archivo, que es lo que
// ocurre cuando la app se cierra y se vuelve a abrir. Si los datos solo vivieran en memoria,
// la segunda instancia los vería vacíos.
//
// (Cerrar la base no es lo mismo que matar el proceso del sistema. Esa parte se comprueba a mano en
// el dispositivo: modo avión, "Forzar detención" y reabrir; ver el recorrido de la tarea.)
@RunWith(AndroidJUnit4::class)
class FavoritePersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    @After
    fun deleteTestDatabase() {
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun favorites_surviveClosingAndReopeningTheDatabase() = runBlocking {
        // "Primera ejecución": se descarga la lista y el usuario marca dos Pokémon
        val firstRun = openDatabase()
        firstRun.pokemonDao().upsertAll(listOf(summary(1), summary(25), summary(4)))
        val firstRepository = FavoriteRepositoryImpl(firstRun.favoriteDao())
        firstRepository.toggle(25)
        firstRepository.toggle(1)
        firstRun.close()

        // "Segunda ejecución": instancia nueva sobre el mismo archivo
        val secondRun = openDatabase()
        try {
            val repository = FavoriteRepositoryImpl(secondRun.favoriteDao())

            assertEquals(listOf(1, 25), repository.observeFavoritePokemon().first().map { it.id })
            assertTrue(repository.observeIsFavorite(25).first())
        } finally {
            secondRun.close()
        }
    }

    @Test
    fun favorites_surviveARefreshAfterReopeningTheDatabase() = runBlocking {
        val firstRun = openDatabase()
        firstRun.pokemonDao().upsertAll(listOf(summary(1), summary(25)))
        FavoriteRepositoryImpl(firstRun.favoriteDao()).toggle(25)
        firstRun.close()

        // Se reabre y llega un refresh de PokéAPI (resumen y ficha) con datos actualizados
        val secondRun = openDatabase()
        try {
            secondRun.pokemonDao().upsertAll(listOf(summary(1), summary(25, name = "pikachu-actualizado")))
            secondRun.detailDao().saveDetail(summary(25, name = "pikachu-actualizado"), detail(25), emptyList())

            val favorites = FavoriteRepositoryImpl(secondRun.favoriteDao()).observeFavoritePokemon().first()

            // El favorito sigue marcado y ahora muestra los datos refrescados
            assertEquals(listOf(25), favorites.map { it.id })
            assertEquals("pikachu-actualizado", favorites.single().name)
        } finally {
            secondRun.close()
        }
    }

    // Misma construcción que la de producción (DatabaseModule): archivo real y todas las migraciones
    private fun openDatabase(): PokedexDatabase =
        Room.databaseBuilder(context, PokedexDatabase::class.java, DB_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            .build()

    private fun summary(id: Int, name: String = "pokemon-$id") =
        PokemonEntity(id = id, name = name, imageUrl = "https://example.com/$id.png")

    private fun detail(id: Int) = PokemonDetailEntity(
        pokemonId = id,
        heightDm = 4,
        weightHg = 60,
        typesJson = """["electric"]""",
        abilitiesJson = """[]""",
        lastUpdated = 1_000L,
    )

    private companion object {
        const val DB_NAME = "favorites-persistence-test"
    }
}
