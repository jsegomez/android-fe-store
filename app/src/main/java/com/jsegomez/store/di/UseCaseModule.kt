package com.jsegomez.store.di

import com.jsegomez.store.features.pokedex.domain.usecase.ObserveIsFavoriteUseCase
import com.jsegomez.store.features.pokedex.domain.usecase.ToggleFavoriteUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

// Los casos de uso no guardan estado, así que son factory: son baratos de crear
val useCaseModule = module {
    factoryOf(::ToggleFavoriteUseCase)
    factoryOf(::ObserveIsFavoriteUseCase)
}
