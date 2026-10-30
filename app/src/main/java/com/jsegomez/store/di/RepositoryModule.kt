package com.jsegomez.store.di

import com.jsegomez.store.features.pokedex.data.InMemoryFavoriteRepository
import com.jsegomez.store.features.pokedex.data.PokedexRepositoryImpl
import com.jsegomez.store.features.pokedex.domain.FavoriteRepository
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.products.data.InMemoryProductRepository
import com.jsegomez.store.features.products.domain.ProductRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PokedexRepository> { PokedexRepositoryImpl(get()) }
    single<ProductRepository> { InMemoryProductRepository() }

    // single: todas las pantallas deben ver la misma lista de favoritos
    single<FavoriteRepository> { InMemoryFavoriteRepository() }
}
