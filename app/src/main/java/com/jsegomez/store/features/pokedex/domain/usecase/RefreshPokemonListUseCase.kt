package com.jsegomez.store.features.pokedex.domain.usecase

import com.jsegomez.store.features.pokedex.domain.PokedexRepository

// Actualiza la lista local desde PokéAPI. Lo comparten el ViewModel (refresco manual) y el Worker
// (refresco en segundo plano): así los dos piden exactamente la misma página.
// Devuelve Result: el Worker mira el AppError para decidir si reintenta o falla.
class RefreshPokemonListUseCase(private val repository: PokedexRepository) {

    suspend operator fun invoke(): Result<Unit> =
        repository.refreshPokemonList(limit = PAGE_SIZE, offset = 0)

    companion object {
        const val PAGE_SIZE = 20
    }
}
