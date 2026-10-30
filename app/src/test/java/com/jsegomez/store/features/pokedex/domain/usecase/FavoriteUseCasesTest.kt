package com.jsegomez.store.features.pokedex.domain.usecase

import com.jsegomez.store.features.pokedex.data.InMemoryFavoriteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteUseCasesTest {
    private val repository = InMemoryFavoriteRepository()
    private val toggleFavorite = ToggleFavoriteUseCase(repository)
    private val observeIsFavorite = ObserveIsFavoriteUseCase(repository)

    @Test
    fun pokemon_isNotFavorite_byDefault() = runBlocking {
        assertFalse(observeIsFavorite(PIKACHU_ID).first())
    }

    @Test
    fun toggle_marksPokemonAsFavorite() = runBlocking {
        toggleFavorite(PIKACHU_ID)

        assertTrue(observeIsFavorite(PIKACHU_ID).first())
    }

    @Test
    fun toggleTwice_removesPokemonFromFavorites() = runBlocking {
        toggleFavorite(PIKACHU_ID)
        toggleFavorite(PIKACHU_ID)

        assertFalse(observeIsFavorite(PIKACHU_ID).first())
    }

    @Test
    fun toggle_doesNotAffectOtherPokemon() = runBlocking {
        toggleFavorite(PIKACHU_ID)

        assertFalse(observeIsFavorite(BULBASAUR_ID).first())
    }

    private companion object {
        const val PIKACHU_ID = 25
        const val BULBASAUR_ID = 1
    }
}
