package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Una fila de "favorites" = un Pokémon que el usuario marcó como favorito.
//
// DECISIÓN DE DISEÑO: esta tabla es independiente de la caché y NO tiene clave foránea.
// - La caché ("pokemon", "pokemon_details", "pokemon_stats") se puede reconstruir en cualquier momento
//   descargándola de nuevo. Los favoritos NO: son datos que creó el usuario y no existen en ningún
//   otro lugar, así que ninguna operación sobre la caché debe poder borrarlos.
// - Con una clave foránea hacia "pokemon" y ON DELETE CASCADE, borrar o reemplazar un resumen de la
//   caché eliminaría también el favorito. Sin clave foránea eso es imposible por construcción.
// - Lo único que se pierde por no tener FK es la garantía de que el Pokémon exista en "pokemon".
//   No es un problema: solo se puede marcar desde el detalle, y guardar una ficha también guarda su
//   resumen. Si algún día faltara el resumen, el favorito sigue guardado y reaparece en el filtro
//   en cuanto el resumen vuelva a descargarse (ver FavoriteDao.observeFavoritePokemon).
@Entity(tableName = "favorites")
data class FavoriteEntity(
    // El id de PokéAPI del Pokémon (el mismo de pokemon.id). Es la clave: no se puede marcar dos veces.
    @PrimaryKey val pokemonId: Int,
)
