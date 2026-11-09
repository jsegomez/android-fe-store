package com.jsegomez.store.features.pokedex.data

import com.jsegomez.store.features.pokedex.data.local.FavoriteDao
import com.jsegomez.store.features.pokedex.data.mapper.toDomain
import com.jsegomez.store.features.pokedex.domain.FavoriteRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Los favoritos viven solo en el teléfono (Room): PokéAPI no recibe nada de esto.
// No hay refresh ni red aquí, a propósito: ninguna descarga de la caché puede tocarlos.
class FavoriteRepositoryImpl(private val favoriteDao: FavoriteDao) : FavoriteRepository {

    override fun observeIsFavorite(pokemonId: Int): Flow<Boolean> =
        favoriteDao.observeIsFavorite(pokemonId)

    override fun observeFavoritePokemon(): Flow<List<PokemonSummary>> =
        favoriteDao.observeFavoritePokemon().map { rows -> rows.map { it.toDomain() } }

    override suspend fun toggle(pokemonId: Int) = favoriteDao.toggle(pokemonId)
}
