package com.jsegomez.store.di

import com.jsegomez.store.features.pokedex.data.PokedexRepositoryImpl
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.products.data.InMemoryProductRepository
import com.jsegomez.store.features.products.domain.ProductRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PokedexRepository> { PokedexRepositoryImpl(get()) }
    single<ProductRepository> { InMemoryProductRepository() }
}
