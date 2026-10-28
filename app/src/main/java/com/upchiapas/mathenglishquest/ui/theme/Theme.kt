package com.upchiapas.mathenglishquest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/** Acceso corto a los tokens: AppTheme.colors.purple, AppTheme.colors.line, etc. */
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun MathEnglishQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val c = if (darkTheme) DarkAppColors else LightAppColors

    // Material 3 recibe los mismos tokens para que sus componentes (TextField, Switch...) combinen.
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = c.blue, onPrimary = Color.White,
            secondary = c.purple, onSecondary = Color.White,
            tertiary = c.green, onTertiary = Color.White,
            background = c.background, onBackground = c.ink,
            surface = c.surface, onSurface = c.ink,
            surfaceVariant = c.line, onSurfaceVariant = c.mute,
            outline = c.line, outlineVariant = c.lineStrong,
            error = c.red, onError = Color.White,
            primaryContainer = c.blueSoft, onPrimaryContainer = c.blueSoftText,
            secondaryContainer = c.purpleSoft, onSecondaryContainer = c.purpleSoftText,
        )
    } else {
        lightColorScheme(
            primary = c.blue, onPrimary = Color.White,
            secondary = c.purple, onSecondary = Color.White,
            tertiary = c.green, onTertiary = Color.White,
            background = c.background, onBackground = c.ink,
            surface = c.surface, onSurface = c.ink,
            surfaceVariant = c.line, onSurfaceVariant = c.mute,
            outline = c.line, outlineVariant = c.lineStrong,
            error = c.red, onError = Color.White,
            primaryContainer = c.blueSoft, onPrimaryContainer = c.blueSoftText,
            secondaryContainer = c.purpleSoft, onSecondaryContainer = c.purpleSoftText,
        )
    }

    CompositionLocalProvider(LocalAppColors provides c) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
