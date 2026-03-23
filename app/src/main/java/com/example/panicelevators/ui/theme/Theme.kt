// ui/theme/Theme.kt
package com.example.panicelevators.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

// ── Light scheme ──────────────────────────────────────────────────────────────
private val LiftCodeLightColors = lightColorScheme(
    // Primary — azul marino
    primary                = Navy30,
    onPrimary              = Grey99,
    primaryContainer       = Navy90,
    onPrimaryContainer     = Navy10,

    // Secondary — cyan
    secondary              = Cyan40,
    onSecondary            = Grey99,
    secondaryContainer     = Cyan90,
    onSecondaryContainer   = Cyan10,

    // Tertiary — azul acero (usado en ADVERTENCIA)
    tertiary               = Steel40,
    onTertiary             = Grey99,
    tertiaryContainer      = Steel90,
    onTertiaryContainer    = Navy10,

    // Error
    error                  = Red40,
    onError                = Grey99,
    errorContainer         = Red90,
    onErrorContainer       = Red10,

    // Superficie
    background             = Navy99,
    onBackground           = Grey10,
    surface                = Navy99,
    onSurface              = Grey10,
    surfaceVariant         = Navy95,
    onSurfaceVariant       = Navy40,
    surfaceContainerLow    = Grey95,

    // Outline
    outline                = Navy80,
    outlineVariant         = Navy90,
)

// ── Dark scheme ───────────────────────────────────────────────────────────────
private val LiftCodeDarkColors = darkColorScheme(
    // Primary
    primary                = Navy80,
    onPrimary              = Navy20,
    primaryContainer       = Navy30,
    onPrimaryContainer     = Navy90,

    // Secondary — cyan
    secondary              = Cyan80,
    onSecondary            = Cyan20,
    secondaryContainer     = Cyan30,
    onSecondaryContainer   = Cyan90,

    // Tertiary
    tertiary               = Steel80,
    onTertiary             = Navy20,
    tertiaryContainer      = Steel40,
    onTertiaryContainer    = Steel90,

    // Error
    error                  = Red80,
    onError                = Red20,
    errorContainer         = Red20,
    onErrorContainer       = Red90,

    // Superficie
    background             = Navy10,
    onBackground           = Grey90,
    surface                = Navy10,
    onSurface              = Grey90,
    surfaceVariant         = Navy20,
    onSurfaceVariant       = Navy80,
    surfaceContainerLow    = Grey20,

    // Outline
    outline                = Navy40,
    outlineVariant         = Navy30,
)

@Composable
fun PanicElevatorsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LiftCodeDarkColors else LiftCodeLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}