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
