package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Punto de entrada a la base de datos local del Pokédex (archivo "pokedex.db" en el teléfono).
//
// - entities: las tablas que forman el esquema.
//     pokemon          -> resumen que usa la lista
//     pokemon_details  -> ficha completa ya descargada
//     pokemon_stats    -> estadísticas de cada ficha
//     favorites        -> Pokémon marcados por el usuario (independiente de la caché)
// - version: identifica la ESTRUCTURA de las tablas (no tiene relación con la versión de PokéAPI).
//   Cada vez que cambie una tabla hay que subirla y escribir una migración (ver PokedexMigrations.kt).
//     v1 -> lista, fichas y estadísticas
//     v2 -> pokemon_details.lastUpdated
//     v3 -> tabla favorites
// - exportSchema: guarda un JSON por versión en app/schemas, necesario para probar migraciones.
@Database(
    entities = [
        PokemonEntity::class,
        PokemonDetailEntity::class,
        PokemonStatEntity::class,
        FavoriteEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun detailDao(): PokemonDetailDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val NAME = "pokedex.db"
    }
}
