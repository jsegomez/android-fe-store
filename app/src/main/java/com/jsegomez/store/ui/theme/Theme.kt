package com.jsegomez.store.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = onPrimaryDark,
    background = BackgroundBottom,
    onBackground = TextPrimaryDark,
    surface = BackgroundBottom,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = onPrimaryLight,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightBackground,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
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
        CompositionLocalProvider(LocalContentColor provides colorScheme.onBackground) {
            Box(modifier = Modifier.fillMaxSize().then(backgroundModifier)) {
                content()
            }
        }
    }
}
