package com.jsegomez.store.features.pokedex.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Una migración transforma una base YA INSTALADA de una versión del esquema a la siguiente,
// conservando los datos. Una instalación nueva no la ejecuta (crea el esquema actual directamente);
// por eso compilar e instalar de cero no demuestra que una migración funcione: eso lo comprueba
// PokedexMigrationTest, que parte de una base antigua con datos reales.

// v1 -> v2: la ficha pasa a guardar cuándo se descargó (lastUpdated).
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // NOT NULL exige un valor en TODAS las filas, y las fichas anteriores no tienen fecha:
        // DEFAULT 0 se la da y significa "fecha desconocida". Debe coincidir con el
        // @ColumnInfo(defaultValue = "0") de PokemonDetailEntity.
        db.execSQL("ALTER TABLE pokemon_details ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT 0")
    }
}

// v2 -> v3: nueva tabla de favoritos. Solo CREA una tabla: no toca pokemon, pokemon_details ni
// pokemon_stats, así que fichas, estadísticas y lastUpdated quedan exactamente como estaban.
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Debe ser equivalente a lo que Room espera para FavoriteEntity (ver 3.json)
        db.execSQL("CREATE TABLE IF NOT EXISTS `favorites` (`pokemonId` INTEGER NOT NULL, PRIMARY KEY(`pokemonId`))")
    }
}

// TODAS las migraciones, en un solo lugar. La base de datos real y las pruebas usan esta lista,
// así que al agregar una migración nueva basta con sumarla aquí: no hay otro sitio que recordar.
// (Una base v1 pasa por 1->2 y luego 2->3 automáticamente.)
val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
