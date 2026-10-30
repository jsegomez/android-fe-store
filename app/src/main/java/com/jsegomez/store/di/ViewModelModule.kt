package com.jsegomez.store.di

import com.jsegomez.store.features.auth.presentation.login.LoginViewModel
import com.jsegomez.store.features.auth.presentation.register.RegisterViewModel
import com.jsegomez.store.features.pokedex.presentation.detail.PokemonDetailViewModel
import com.jsegomez.store.features.pokedex.presentation.pokedex.PokedexViewModel
import com.jsegomez.store.features.products.presentation.list.ProductListViewModel
import com.jsegomez.store.features.products.presentation.register.ProductRegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::PokedexViewModel)
    viewModelOf(::PokemonDetailViewModel)
    viewModelOf(::ProductListViewModel)
    viewModelOf(::ProductRegisterViewModel)
}
