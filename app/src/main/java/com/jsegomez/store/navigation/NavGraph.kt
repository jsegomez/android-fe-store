package com.jsegomez.store.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.jsegomez.store.features.auth.navigation.authGraph
import com.jsegomez.store.features.auth.navigation.navigateToMainAfterLogin
import com.jsegomez.store.features.home.navigation.Home
import com.jsegomez.store.features.home.navigation.homeGraph
import com.jsegomez.store.features.pokedex.navigation.navigateToPokedex
import com.jsegomez.store.features.pokedex.navigation.navigateToPokemonDetail
import com.jsegomez.store.features.pokedex.navigation.pokedexGraph
import com.jsegomez.store.features.products.navigation.navigateToProductList
import com.jsegomez.store.features.products.navigation.navigateToProductListAfterSave
import com.jsegomez.store.features.products.navigation.navigateToProductRegister
import com.jsegomez.store.features.products.navigation.productsGraph

private const val NAV_ANIM_MS = 300

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Auth,
        modifier = modifier,
        // Deslizamiento horizontal: al avanzar entra por la derecha,
        // al regresar la pantalla actual sale hacia la derecha
        enterTransition = {
            slideInHorizontally(tween(NAV_ANIM_MS)) { it } + fadeIn(tween(NAV_ANIM_MS))
        },
        exitTransition = {
            slideOutHorizontally(tween(NAV_ANIM_MS)) { -it / 4 } + fadeOut(tween(NAV_ANIM_MS))
        },
        popEnterTransition = {
            slideInHorizontally(tween(NAV_ANIM_MS)) { -it / 4 } + fadeIn(tween(NAV_ANIM_MS))
        },
        popExitTransition = {
            slideOutHorizontally(tween(NAV_ANIM_MS)) { it } + fadeOut(tween(NAV_ANIM_MS))
        },
        // Gesto de atrás: por defecto usa scaleOut(0.7f) (la pantalla se encoge)
        predictivePopEnterTransition = {
            slideInHorizontally(tween(NAV_ANIM_MS)) { -it / 4 } + fadeIn(tween(NAV_ANIM_MS))
        },
        predictivePopExitTransition = {
            slideOutHorizontally(tween(NAV_ANIM_MS)) { it } + fadeOut(tween(NAV_ANIM_MS))
        }
    ) {
        authGraph(onLoginSuccess = navController::navigateToMainAfterLogin)
        navigation<Main>(startDestination = Home) {
            homeGraph(
                onNavigateToProductRegister = navController::navigateToProductRegister,
                onNavigateToProductList = navController::navigateToProductList,
                onNavigateToPokedex = navController::navigateToPokedex
            )
            productsGraph(
                onProductSaved = navController::navigateToProductListAfterSave,
                onBack = { navController.popBackStack() }
            )
            pokedexGraph(
                onBack = { navController.popBackStack() },
                onPokemonClick = navController::navigateToPokemonDetail
            )
        }
    }
}
