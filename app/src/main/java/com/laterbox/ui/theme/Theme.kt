package com.laterbox.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.laterbox.data.model.ThemeMode

@Immutable
data class ExtendedColors(
    val border: Color,
    val amberAccent: Color,
    val amberAccentSubtle: Color,
    val warmError: Color,
    val textSecondary: Color,
    val cardBackground: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        border = WarmBorderLight,
        amberAccent = AmberAccent,
        amberAccentSubtle = AmberAccentSubtleLight,
        warmError = WarmTerracotta,
        textSecondary = WarmTextSecondaryLight,
        cardBackground = WarmSurfaceLight
    )
}

private val LightColorScheme = lightColorScheme(
    primary = AmberAccent,
    onPrimary = NeutralWhite,
    primaryContainer = AmberAccentSubtleLight,
    onPrimaryContainer = AmberAccent,
    secondary = WarmTextSecondaryLight,
    onSecondary = NeutralWhite,
    secondaryContainer = WarmSurfaceVariantLight,
    onSecondaryContainer = WarmTextPrimaryLight,
    background = WarmBackgroundLight,
    onBackground = WarmTextPrimaryLight,
    surface = WarmSurfaceLight,
    onSurface = WarmTextPrimaryLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onSurfaceVariant = WarmTextSecondaryLight,
    outline = WarmBorderLight,
    outlineVariant = WarmBorderLight,
    error = WarmTerracotta,
    onError = NeutralWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberAccentLight,
    onPrimary = CharcoalBackgroundDark,
    primaryContainer = AmberAccentSubtleDark,
    onPrimaryContainer = AmberAccentLight,
    secondary = WarmTextSecondaryDark,
    onSecondary = NeutralWhite,
    secondaryContainer = CharcoalSurfaceVariantDark,
    onSecondaryContainer = WarmTextPrimaryDark,
    background = CharcoalBackgroundDark,
    onBackground = WarmTextPrimaryDark,
    surface = CharcoalSurfaceDark,
    onSurface = WarmTextPrimaryDark,
    surfaceVariant = CharcoalSurfaceVariantDark,
    onSurfaceVariant = WarmTextSecondaryDark,
    outline = CharcoalBorderDark,
    outlineVariant = CharcoalBorderDark,
    error = WarmTerracottaDark,
    onError = NeutralWhite
)

@Composable
fun LaterBoxTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (darkTheme) {
        ExtendedColors(
            border = CharcoalBorderDark,
            amberAccent = AmberAccentLight,
            amberAccentSubtle = AmberAccentSubtleDark,
            warmError = WarmTerracottaDark,
            textSecondary = WarmTextSecondaryDark,
            cardBackground = CharcoalSurfaceDark
        )
    } else {
        ExtendedColors(
            border = WarmBorderLight,
            amberAccent = AmberAccent,
            amberAccentSubtle = AmberAccentSubtleLight,
            warmError = WarmTerracotta,
            textSecondary = WarmTextSecondaryLight,
            cardBackground = WarmSurfaceLight
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object LaterBoxThemeTokens {
    val extended: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
