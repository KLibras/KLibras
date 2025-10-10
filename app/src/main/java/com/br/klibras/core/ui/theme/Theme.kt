package com.br.klibras.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HighlightYellow,
    onPrimary = Black100,

    background = Grey100,
    onBackground = White,

    surface = Black100,
    onSurface = White,

    secondary = Grey70,
    tertiary = Grey50
)

private val LightColorScheme = lightColorScheme(
    primary = HighlightYellow,
    onPrimary = Black100,

    background = Beige,
    onBackground = Black100,

    surface = White,
    onSurface = Black100,

    secondary = Grey50,
    tertiary = Grey70
)

@Composable
fun KLibrasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}