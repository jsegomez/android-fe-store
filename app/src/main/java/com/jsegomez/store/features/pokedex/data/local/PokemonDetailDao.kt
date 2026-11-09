package com.jsegomez.store.features.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Es una clase abstracta (y no una interfaz) porque necesita un método con código propio:
// saveDetail. Room permite métodos con cuerpo en los DAO solo si son clases abstractas.
@Dao
abstract class PokemonDetailDao {

    // Devuelve null cuando la ficha NO está guardada (aunque exista el resumen en "pokemon").
    // Así la pantalla distingue "ficha todavía no descargada" de cualquier otro estado.
    // @Transaction: Room necesita varias consultas para armar el resultado (ficha, resumen y
    // estadísticas); la transacción asegura que las lea como una foto coherente.
    @Transaction
    @Query("SELECT * FROM pokemon_details WHERE pokemonId = :pokemonId")
    abstract fun observe(pokemonId: Int): Flow<CachedPokemonDetail?>

    // Guarda una ficha completa: resumen + ficha + estadísticas, TODO o NADA.
    // Si algo falla a la mitad, Room deshace lo ya escrito y queda la ficha anterior.
    // El orden importa por las claves foráneas: primero el padre, luego los hijos.
    @Transaction
    open suspend fun saveDetail(
        summary: PokemonEntity,
        detail: PokemonDetailEntity,
        stats: List<PokemonStatEntity>,
    ) {
        upsertSummary(summary)
        upsertDetail(detail)
        // Se reemplaza el conjunto entero: así no sobrevive una estadística que la API ya no trae
        deleteStats(detail.pokemonId)
        insertStats(stats)
    }

    // Piezas internas de saveDetail. Son protected para que nadie las llame por separado
    // fuera de la transacción (por ejemplo, borrar las estadísticas sin volver a insertarlas).
    // Upsert del resumen (y no REPLACE): REPLACE borra y reinserta la fila, y con CASCADE
    // eso eliminaría la ficha que acabamos de guardar.
    @Upsert
    protected abstract suspend fun upsertSummary(summary: PokemonEntity)

    @Upsert
    protected abstract suspend fun upsertDetail(detail: PokemonDetailEntity)

    @Query("DELETE FROM pokemon_stats WHERE pokemonId = :pokemonId")
    protected abstract suspend fun deleteStats(pokemonId: Int)

    @Insert
    protected abstract suspend fun insertStats(stats: List<PokemonStatEntity>)
}
