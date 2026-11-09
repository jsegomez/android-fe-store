package com.jsegomez.store.features.pokedex.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Una fila de "pokemon_details" = la ficha COMPLETA de un Pokémon ya descargada.
//
// Es una tabla aparte de "pokemon" a propósito: la lista solo trae id y nombre, así que tener
// la fila de bulbasaur en "pokemon" NO significa que su ficha esté guardada. Si aquí no hay fila,
// la pantalla de detalle sabe que esa ficha todavía no se ha descargado.
@Entity(
    tableName = "pokemon_details",
    foreignKeys = [
        ForeignKey(
            entity = PokemonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pokemonId"],
            // Si algún día se borra el resumen de la caché, su ficha ya no tiene sentido sin él.
            // Es seguro porque el refresh usa Upsert y nunca borra resúmenes.
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PokemonDetailEntity(
    // Es la clave primaria Y apunta al resumen: un Pokémon tiene, como mucho, una ficha.
    @PrimaryKey val pokemonId: Int,
    // Se guardan en las unidades de PokéAPI (decímetros y hectogramos); se dividen entre 10
    // al convertir a metros y kilogramos, justo antes de mostrarlos.
    val heightDm: Int,
    val weightHg: Int,
    // Listas pequeñas que siempre se leen completas y nunca se consultan por separado
    // (no buscamos "todos los de tipo fuego"), por eso van como texto JSON en vez de tablas.
    // El formato lo definen los mappers: ver PokemonDetailLocalMapper.kt
    val typesJson: String,
    val abilitiesJson: String,
    // Cuándo se descargó esta ficha: milisegundos desde 1970 (System.currentTimeMillis()).
    // Es la fecha del TELÉFONO, no la de modificación en PokéAPI: si el reloj cambia, cambia su lectura.
    // 0 significa "no se sabe": lo tienen las fichas guardadas antes de la v2, hasta que se vuelvan a descargar.
    // Vive aquí y no en "pokemon" porque describe a la ficha: refrescar la lista no debe hacer
    // parecer reciente una ficha antigua.
    // defaultValue debe coincidir con el DEFAULT del SQL de MIGRATION_1_2; si no, Room rechaza
    // el esquema migrado. No lleva valor por defecto en Kotlin para que quien cree una ficha
    // tenga que decidir la fecha, en vez de dejar un 0 por olvido.
    @ColumnInfo(defaultValue = "0")
    val lastUpdated: Long,
)
