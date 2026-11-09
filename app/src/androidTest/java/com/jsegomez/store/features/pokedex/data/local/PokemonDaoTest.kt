package com.jsegomez.store.features.pokedex.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Prueba del DAO contra una base real de Room, pero en MEMORIA: se crea limpia para cada test
// y desaparece al cerrarse. Sirve para verificar Upsert y orden; NO demuestra que los datos
// sobrevivan a un reinicio (eso lo cubre la prueba de persistencia, con la base en disco).
@RunWith(AndroidJUnit4::class)
class PokemonDaoTest {
    private lateinit var database: PokedexDatabase
    private lateinit var dao: PokemonDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokedexDatabase::class.java).build()
        dao = database.pokemonDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun observeAll_isEmpty_whenNothingWasSaved() = runBlocking {
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun observeAll_returnsPokemonOrderedById() = runBlocking {
        // Se insertan desordenados a propósito: el orden debe venir de la consulta, no de la inserción
        dao.upsertAll(listOf(pokemon(id = 25), pokemon(id = 1), pokemon(id = 4)))

        val ids = dao.observeAll().first().map { it.id }

        assertEquals(listOf(1, 4, 25), ids)
    }

    @Test
    fun upsertAll_updatesExistingRow_insteadOfDuplicatingIt() = runBlocking {
        dao.upsertAll(listOf(pokemon(id = 1, name = "bulbasaur")))

        // Misma clave (id = 1) con otro nombre: simula que un refresh trae datos distintos
        dao.upsertAll(listOf(pokemon(id = 1, name = "bulbasaur-actualizado")))

        val saved = dao.observeAll().first()
        assertEquals(1, saved.size)
        assertEquals("bulbasaur-actualizado", saved.single().name)
    }

    @Test
    fun upsertAll_keepsPokemonMissingFromTheNewResponse() = runBlocking {
        dao.upsertAll(listOf(pokemon(id = 1), pokemon(id = 2)))

        // La segunda respuesta solo trae el id 1: el id 2 no debe borrarse
        dao.upsertAll(listOf(pokemon(id = 1)))

        assertEquals(listOf(1, 2), dao.observeAll().first().map { it.id })
    }

    private fun pokemon(id: Int, name: String = "pokemon-$id") =
        PokemonEntity(id = id, name = name, imageUrl = "https://example.com/$id.png")
}
