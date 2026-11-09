package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    // Lectura OBSERVABLE: devuelve un Flow, por eso no lleva "suspend". Room vuelve a emitir
    // la lista completa cada vez que cambia la tabla, así la pantalla se actualiza sola.
    // ORDER BY id mantiene el orden numérico de la Pokédex (#001, #002...).
    @Query("SELECT * FROM pokemon ORDER BY id")
    fun observeAll(): Flow<List<PokemonEntity>>

    // Upsert = inserta si el id no existe, actualiza si ya existe.
    // Es lo que permite refrescar la misma página varias veces sin duplicar filas
    // y sin borrar nada: una respuesta de PokéAPI nunca elimina lo que ya teníamos.
    @Upsert
    suspend fun upsertAll(pokemon: List<PokemonEntity>)
}
