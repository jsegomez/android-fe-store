package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// Clase abstracta (y no interfaz) porque toggle necesita código propio; ver PokemonDetailDao.
@Dao
abstract class FavoriteDao {

    // Para el corazón del detalle: emite true/false y se actualiza solo cuando cambia la tabla
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE pokemonId = :pokemonId)")
    abstract fun observeIsFavorite(pokemonId: Int): Flow<Boolean>

    // Para el filtro "Solo favoritos": los RESÚMENES de los Pokémon marcados, listos para la lista.
    // INNER JOIN = solo aparecen los favoritos cuyo resumen está en la caché (ver FavoriteEntity).
    // Es la misma forma de dato que PokemonDao.observeAll, así la pantalla pinta ambas igual.
    @Query(
        """
        SELECT pokemon.* FROM pokemon
        INNER JOIN favorites ON favorites.pokemonId = pokemon.id
        ORDER BY pokemon.id
        """
    )
    abstract fun observeFavoritePokemon(): Flow<List<PokemonEntity>>

    // Marca el Pokémon si no era favorito, o lo desmarca si ya lo era.
    // @Transaction: "preguntar y luego escribir" tiene que ser UNA sola operación. Sin ella, dos
    // toques muy seguidos podrían leer el mismo estado y ambos intentar insertar (error de clave duplicada).
    @Transaction
    open suspend fun toggle(pokemonId: Int) {
        if (isFavorite(pokemonId)) {
            delete(pokemonId)
        } else {
            insert(FavoriteEntity(pokemonId))
        }
    }

    // Piezas internas de toggle; protected para no usarlas por separado fuera de la transacción
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE pokemonId = :pokemonId)")
    protected abstract suspend fun isFavorite(pokemonId: Int): Boolean

    @Insert
    protected abstract suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE pokemonId = :pokemonId")
    protected abstract suspend fun delete(pokemonId: Int)
}
