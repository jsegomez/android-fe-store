package com.jsegomez.store.features.pokedex.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.jsegomez.store.features.pokedex.presentation.pokedex.PokedexScreen
import com.jsegomez.store.navigation.Pokedex

fun NavGraphBuilder.pokedexGraph(
    onBack: () -> Unit
) {
    navigation<Pokedex>(startDestination = PokedexRoute) {
        composable<PokedexRoute> { PokedexScreen(onBack = onBack) }
    }
}