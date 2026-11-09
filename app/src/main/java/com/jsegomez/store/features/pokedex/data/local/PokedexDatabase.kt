package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Punto de entrada a la base de datos local del Pokédex (archivo "pokedex.db" en el teléfono).
//
// - entities: las tablas que forman el esquema. Hoy solo la lista; el detalle se agrega en la Fase B.
// - version: identifica la ESTRUCTURA de las tablas (no tiene relación con la versión de PokéAPI).
//   Cada vez que cambie una tabla hay que subirla y escribir una migración.
// - exportSchema: guarda un JSON por versión en app/schemas, necesario para probar migraciones.
@Database(
    entities = [PokemonEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao

    companion object {
        const val NAME = "pokedex.db"
    }
}
