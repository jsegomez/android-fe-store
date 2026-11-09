package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Embedded
import androidx.room.Relation

// NO es una tabla: es el resultado de leer una ficha junto con sus datos relacionados.
// Room arma este objeto con varias consultas (por eso el DAO usa @Transaction al leerlo).
//
// @Relation solo le dice a Room cómo juntar los datos al LEER. La integridad de los datos
// (que no exista una estadística sin ficha) la garantizan las claves foráneas de las entidades.
data class CachedPokemonDetail(
    // @Embedded: las columnas de la ficha se incorporan directamente a este resultado
    @Embedded val detail: PokemonDetailEntity,

    // El resumen del mismo Pokémon: de ahí salen el nombre y la imagen
    @Relation(parentColumn = "pokemonId", entityColumn = "id")
    val pokemon: PokemonEntity,

    // Todas las estadísticas de la ficha (sin orden garantizado; ver PokemonStatEntity.position)
    @Relation(parentColumn = "pokemonId", entityColumn = "pokemonId")
    val stats: List<PokemonStatEntity>,
)
