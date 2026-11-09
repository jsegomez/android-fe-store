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

    // ---------- v1 -> v2: lastUpdated ----------

    @Test
    fun migrate1To2_keepsEverythingSaved_andMarksTheDownloadDateAsUnknown() {
        createVersion1DatabaseWithBulbasaur()

        // validateDroppedTables = true: además de ejecutar la migración, comprueba que el esquema
        // resultante sea IDÉNTICO al de 2.json (es decir, al de una instalación nueva de la v2)
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        assertBulbasaurKept(db)
        // Lo único nuevo: la ficha antigua queda con fecha 0 = "se desconoce cuándo se descargó"
        assertEquals(0L, db.longOf("SELECT lastUpdated FROM pokemon_details WHERE pokemonId = 1"))
    }

    // ---------- v2 -> v3: favoritos ----------

    @Test
    fun migrate2To3_keepsEverythingSaved_andAddsAnEmptyFavoritesTable() {
        createVersion2DatabaseWithBulbasaur()

        // Valida contra 3.json: el esquema migrado es idéntico al de una instalación nueva de la v3
        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        assertBulbasaurKept(db)
        // Fichas, estadísticas Y la fecha de actualización siguen exactamente como estaban
        assertEquals(DOWNLOADED_AT, db.longOf("SELECT lastUpdated FROM pokemon_details WHERE pokemonId = 1"))
        // La tabla nueva existe, está vacía y se puede escribir
        assertEquals(0L, db.longOf("SELECT COUNT(*) FROM favorites"))
        db.execSQL("INSERT INTO favorites (pokemonId) VALUES (1)")
        assertEquals(1L, db.longOf("SELECT COUNT(*) FROM favorites"))
    }

    @Test
    fun migrate1To3_runsBothMigrationsInOrder() {
        // Un usuario que sigue en la v1 salta directo a la v3: Room encadena 1->2 y 2->3
        createVersion1DatabaseWithBulbasaur()

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, *ALL_MIGRATIONS)

        assertBulbasaurKept(db)
        assertEquals(0L, db.longOf("SELECT lastUpdated FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(0L, db.longOf("SELECT COUNT(*) FROM favorites"))
    }

    // ---------- La base migrada funciona con el código real ----------

    @Test
    fun migratedDatabase_canBeReadAndWrittenWithTheRealDaos() = runBlocking {
        createVersion1DatabaseWithBulbasaur()

        // Se abre con Room de verdad (no con SQL a mano), como lo hace la app, y se usan los DAO reales:
        // así se comprueba que las consultas funcionan sobre el esquema ya migrado
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.databaseBuilder(context, PokedexDatabase::class.java, TEST_DB)
            .addMigrations(*ALL_MIGRATIONS)
            .build()
        try {
            val cached = database.detailDao().observe(1).first()
            assertNotNull(cached)
            assertEquals("bulbasaur", cached!!.pokemon.name)
            assertEquals(listOf("hp", "attack"), cached.stats.sortedBy { it.position }.map { it.name })
            assertEquals(0L, cached.detail.lastUpdated)

            // Y los favoritos funcionan sobre los datos que ya existían
            database.favoriteDao().toggle(1)
            assertEquals(listOf(1), database.favoriteDao().observeFavoritePokemon().first().map { it.id })
        } finally {
            database.close()
        }
    }

    // ---------- Bases antiguas de ejemplo ----------

    // Una base v1 con lo que una app real habría guardado: la lista, la ficha de bulbasaur y sus estadísticas
    private fun createVersion1DatabaseWithBulbasaur() {
        val db = helper.createDatabase(TEST_DB, 1)
        db.execSQL("INSERT INTO pokemon (id, name, imageUrl) VALUES (1, 'bulbasaur', 'https://example.com/1.png')")
        db.execSQL(
            "INSERT INTO pokemon_details (pokemonId, heightDm, weightHg, typesJson, abilitiesJson) " +
                "VALUES (1, 7, 69, '$TYPES_JSON', '$ABILITIES_JSON')"
        )
        insertBulbasaurStats(db)
        // Hay que cerrarla antes de migrar
        db.close()
    }

    // Lo mismo en la v2, que ya tiene lastUpdated. Se usa una fecha real (distinta de 0) para
    // comprobar que la migración 2->3 no la modifica.
    private fun createVersion2DatabaseWithBulbasaur() {
        val db = helper.createDatabase(TEST_DB, 2)
        db.execSQL("INSERT INTO pokemon (id, name, imageUrl) VALUES (1, 'bulbasaur', 'https://example.com/1.png')")
        db.execSQL(
            "INSERT INTO pokemon_details (pokemonId, heightDm, weightHg, typesJson, abilitiesJson, lastUpdated) " +
                "VALUES (1, 7, 69, '$TYPES_JSON', '$ABILITIES_JSON', $DOWNLOADED_AT)"
        )
        insertBulbasaurStats(db)
        db.close()
    }

    // Las estadísticas dependen de la ficha por clave foránea: se insertan siempre DESPUÉS de ella
    private fun insertBulbasaurStats(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO pokemon_stats (pokemonId, name, baseValue, position) VALUES (1, 'hp', 45, 0)")
        db.execSQL("INSERT INTO pokemon_stats (pokemonId, name, baseValue, position) VALUES (1, 'attack', 49, 1)")
    }

    // Comprobaciones comunes: lo que cualquier migración debe dejar intacto
    private fun assertBulbasaurKept(db: SupportSQLiteDatabase) {
        assertEquals(1L, db.longOf("SELECT COUNT(*) FROM pokemon"))
        assertEquals("bulbasaur", db.stringOf("SELECT name FROM pokemon WHERE id = 1"))
        assertEquals(7L, db.longOf("SELECT heightDm FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(69L, db.longOf("SELECT weightHg FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(TYPES_JSON, db.stringOf("SELECT typesJson FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(ABILITIES_JSON, db.stringOf("SELECT abilitiesJson FROM pokemon_details WHERE pokemonId = 1"))
        assertEquals(2L, db.longOf("SELECT COUNT(*) FROM pokemon_stats WHERE pokemonId = 1"))
        assertEquals(49L, db.longOf("SELECT baseValue FROM pokemon_stats WHERE pokemonId = 1 AND name = 'attack'"))
    }

    private fun SupportSQLiteDatabase.longOf(sql: String): Long =
        compileStatement(sql).use { it.simpleQueryForLong() }

    private fun SupportSQLiteDatabase.stringOf(sql: String): String =
        compileStatement(sql).use { requireNotNull(it.simpleQueryForString()) }

    private companion object {
        const val TEST_DB = "migration-test"
        const val TYPES_JSON = """["grass","poison"]"""
        const val ABILITIES_JSON = """[{"name":"overgrow","isHidden":false}]"""
        const val DOWNLOADED_AT = 1_700_000_000_000L
    }
}
