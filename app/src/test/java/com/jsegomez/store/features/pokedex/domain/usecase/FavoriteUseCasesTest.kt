package com.jsegomez.store.features.pokedex.domain.usecase

import com.jsegomez.store.features.pokedex.domain.FakeFavoriteRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteUseCasesTest {
    private val repository = FakeFavoriteRepository(catalog = listOf(BULBASAUR, PIKACHU))
    private val toggleFavorite = ToggleFavoriteUseCase(repository)
    private val observeIsFavorite = ObserveIsFavoriteUseCase(repository)
    private val observeFavoritePokemon = ObserveFavoritePokemonUseCase(repository)

    @Test
    fun pokemon_isNotFavorite_byDefault() = runBlocking {
        assertFalse(observeIsFavorite(PIKACHU.id).first())
    }

    @Test
    fun toggle_marksPokemonAsFavorite() = runBlocking {
        toggleFavorite(PIKACHU.id)

        assertTrue(observeIsFavorite(PIKACHU.id).first())
    }

    @Test
    fun toggleTwice_removesPokemonFromFavorites() = runBlocking {
        toggleFavorite(PIKACHU.id)
        toggleFavorite(PIKACHU.id)

        assertFalse(observeIsFavorite(PIKACHU.id).first())
    }

    @Test
    fun toggle_doesNotAffectOtherPokemon() = runBlocking {
        toggleFavorite(PIKACHU.id)

        assertFalse(observeIsFavorite(BULBASAUR.id).first())
    }

    @Test
    fun observeFavoritePokemon_isEmpty_whenNothingIsMarked() = runBlocking {
        assertTrue(observeFavoritePokemon().first().isEmpty())
    }

    @Test
    fun observeFavoritePokemon_returnsOnlyTheMarkedOnes() = runBlocking {
        toggleFavorite(PIKACHU.id)

        assertEquals(listOf(PIKACHU), observeFavoritePokemon().first())
    }

    @Test
    fun observeFavoritePokemon_afterUnmarking_isEmptyAgain() = runBlocking {
        toggleFavorite(PIKACHU.id)
        toggleFavorite(PIKACHU.id)

        assertTrue(observeFavoritePokemon().first().isEmpty())
    }

    private companion object {
        val BULBASAUR = PokemonSummary(id = 1, name = "bulbasaur", imageUrl = "https://example.com/1.png")
        val PIKACHU = PokemonSummary(id = 25, name = "pikachu", imageUrl = "https://example.com/25.png")
    }
}
