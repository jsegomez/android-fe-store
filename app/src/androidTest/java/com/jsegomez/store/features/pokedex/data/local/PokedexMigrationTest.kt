package com.jsegomez.store.features.pokedex.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Prueba de migraciones: parte de una base ANTIGUA con datos reales y comprueba que, al migrarla,
// no se pierde nada. Una instalación limpia no sirve para esto (no ejecuta la migración).
//
// MigrationTestHelper crea la base con el esquema EXACTO de una versión anterior, leyendo el JSON
// que Room exportó en app/schemas (por eso esos archivos se suben a git y no se editan).
@RunWith(AndroidJUnit4::class)
class PokedexMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PokedexDatabase::class.java,
    )

    @After
    fun deleteTestDatabase() {
        ApplicationProvider.getApplicationContext<Context>().deleteDatabase(TEST_DB)
    }

    @Test
    fun migrate1To2_keepsEverythingSaved_andMarksTheDownloadDateAsUnknown() {
        createVersion1DatabaseWithBulbasaur()

        // validateDroppedTables = true: además de ejecutar la migración, comprueba que el esquema
        // resultante sea IDÉNTICO al de 2.json (es decir, al de una instalación nueva de la v2)
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Los datos que ya había siguen ahí, intactos
        assertEquals(1L, db.longOf("SELECT COUNT(*) FROM pokemon"))
        assertEquals("bulbasaur", db.stringOf("SELECT name FROM pokemon WHERE id = 1"))
        assertEquals(7L, db.longOf("SELECT heightDm FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(69L, db.longOf("SELECT weightHg FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(TYPES_JSON, db.stringOf("SELECT typesJson FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(ABILITIES_JSON, db.stringOf("SELECT abilitiesJson FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(2L, db.longOf("SELECT COUNT(*) FROM pokemon_stats WHERE pokemonId = 1"))
        assertEquals(49L, db.longOf("SELECT baseValue FROM pokemon_stats WHERE pokemonId = 1 AND name = 'attack'"))

        // Lo único nuevo: la ficha antigua queda con fecha 0 = "se desconoce cuándo se descargó"
        assertEquals(0L, db.longOf("SELECT lastUpdated FROM pokemon_details WHERE pokemonId = 1"))
    }

    @Test
    fun migratedDatabase_canBeReadWithTheRealDao() = runBlocking {
        createVersion1DatabaseWithBulbasaur()

        // Se abre la base migrada con Room de verdad (no con SQL a mano) y se lee con el DAO real:
        // así se comprueba que las consultas funcionan sobre el esquema ya migrado
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.databaseBuilder(context, PokedexDatabase::class.java, TEST_DB)
            .addMigrations(MIGRATION_1_2)
            .build()
        try {
            val cached = database.detailDao().observe(1).first()

            assertNotNull(cached)
            assertEquals("bulbasaur", cached!!.pokemon.name)
            assertEquals(listOf("hp", "attack"), cached.stats.sortedBy { it.position }.map { it.name })
            assertEquals(0L, cached.detail.lastUpdated)
        } finally {
            database.close()
        }
    }

    // Una base v1 con lo que una app real habría guardado: la lista, la ficha de bulbasaur y sus estadísticas
    private fun createVersion1DatabaseWithBulbasaur() {
        val db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO pokemon (id, name, imageUrl) VALUES (1, 'bulbasaur', 'https://example.com/1.png')")
        db.execSQL(
            "INSERT INTO pokemon_details (pokemonId, heightDm, weightHg, typesJson, abilitiesJson) " +
                "VALUES (1, 7, 69, '$TYPES_JSON', '$ABILITIES_JSON')"
        )
        db.execSQL("INSERT INTO pokemon_stats (pokemonId, name, baseValue, position) VALUES (1, 'hp', 45, 0)")
        db.execSQL("INSERT INTO pokemon_stats (pokemonId, name, baseValue, position) VALUES (1, 'attack', 49, 1)")
        // Hay que cerrarla antes de migrar
        db.close()
    }

    private fun SupportSQLiteDatabase.longOf(sql: String): Long =
        compileStatement(sql).use { it.simpleQueryForLong() }

    private fun SupportSQLiteDatabase.stringOf(sql: String): String =
        compileStatement(sql).use { requireNotNull(it.simpleQueryForString()) }

    private companion object {
        const val TEST_DB = "migration-test"
        const val TYPES_JSON = """["grass","poison"]"""
        const val ABILITIES_JSON = """[{"name":"overgrow","isHidden":false}]"""
    }
}
