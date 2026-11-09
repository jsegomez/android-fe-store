package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Una fila de "pokemon_stats" = una estadística (hp, attack...) de una ficha.
// Relación 1 a N: una ficha tiene varias estadísticas y cada una pertenece a una sola ficha.
@Entity(
    tableName = "pokemon_stats",
    // Clave compuesta: el "hp" de bulbasaur y el "hp" de ivysaur son filas distintas,
    // pero el mismo par (pokemonId, name) no se puede repetir.
    primaryKeys = ["pokemonId", "name"],
    foreignKeys = [
        ForeignKey(
            entity = PokemonDetailEntity::class,
            parentColumns = ["pokemonId"],
            childColumns = ["pokemonId"],
            // Si se elimina la ficha, sus estadísticas se van con ella
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // Acelera "dame las estadísticas de esta ficha" y evita una advertencia de Room sobre la FK
    indices = [Index("pokemonId")],
)
data class PokemonStatEntity(
    val pokemonId: Int,
    val name: String,
    val baseValue: Int,
    // Room no garantiza el orden de las filas relacionadas. Guardamos la posición original
    // que trajo la API para poder mostrar las estadísticas siempre en el mismo orden.
    val position: Int,
)
