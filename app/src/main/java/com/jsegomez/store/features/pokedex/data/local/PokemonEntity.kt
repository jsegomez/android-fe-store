package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Una fila de la tabla "pokemon" = un resumen de los que muestra la lista del Pokédex.
// Es un modelo de ALMACENAMIENTO: no se usa en la UI (la UI usa PokemonSummary, del dominio).
@Entity(tableName = "pokemon")
data class PokemonEntity(
    // Usamos el id que ya trae PokéAPI (no autogenerado). Así, al refrescar la lista,
    // el mismo Pokémon cae siempre en la misma fila y se actualiza en vez de duplicarse.
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
)
