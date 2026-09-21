package com.jsegomez.store.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = OnMint,
    secondary = SurfaceVariantDark,
    onSecondary = TextPrimary,
    tertiary = PositiveGreen,
    // Solo es el color de respaldo: el fondo real es el degradado de StoreTheme
    background = BackgroundBottom,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightSurfaceVariant,
    onSecondary = LightTextPrimary,
    tertiary = LightPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary
)

private val DarkBackgroundBrush = Brush.verticalGradient(
    colors = listOf(BackgroundTop, BackgroundMid, BackgroundBottom)
)

/**
 * Tema de la app. Pinta el fondo una sola vez (degradado en modo oscuro, color liso en
 * modo claro), por lo que las pantallas NO deben poner su propio `background`; solo deben
 * usar contenedores transparentes (el Scaffold con `containerColor = Color.Transparent`).
 */
@Composable
fun StoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val backgroundModifier = if (darkTheme) {
        Modifier.background(DarkBackgroundBrush)
    } else {
        Modifier.background(colorScheme.background)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        Box(modifier = Modifier.fillMaxSize().then(backgroundModifier)) {
            content()
        }
    }
}
