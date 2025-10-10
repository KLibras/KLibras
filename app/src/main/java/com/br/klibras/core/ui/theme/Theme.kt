package com.br.klibras.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ----------------------------------------------------------------------------------
// AS PALETAS PRECISAM SER COMPLETAS (ELAS USAM AS CORES DEFINIDAS NO Color.kt)
// ----------------------------------------------------------------------------------

private val DarkColorScheme = darkColorScheme(
    primary = HighlightYelow,      // Amarelo para destaque (botões)
    onPrimary = Black100,          // Texto sobre o primário

    background = Grey100,          // <-- CHAVE: Fundo ESCURO para Dark Theme
    onBackground = White,          // <-- CHAVE: Texto CLARO para Dark Theme

    surface = Black100,            // Superfície escura para cards/elementos
    onSurface = White,             // Texto sobre a superfície

    secondary = Grey70,
    tertiary = Grey50
)

private val LightColorScheme = lightColorScheme(
    primary = HighlightYelow,      // Amarelo para destaque
    onPrimary = Black100,          // Texto sobre o primário

    background = Beige,            // <-- CHAVE: Fundo CLARO para Light Theme
    onBackground = Black100,       // <-- CHAVE: Texto ESCURO para Light Theme

    surface = White,               // Superfície clara para cards/elementos
    onSurface = Black100,          // Texto sobre a superfície

    secondary = Grey50,
    tertiary = Grey70
)

@Composable
fun KLibrasTheme(
    // Deixamos 'dynamicColor' como false, pois a lógica de cores acima
    // é a sua paleta customizada, e não as cores do Android 12+.
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Mudança para focar na sua paleta
    content: @Composable () -> Unit
) {
    // A lógica de dynamicColor é removida ou simplificada para o seu caso
    // se você não quiser depender das cores do Android 12+.
    // A lógica básica de alternância entre suas duas paletas é o que importa:

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}