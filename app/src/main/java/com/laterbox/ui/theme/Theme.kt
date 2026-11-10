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
    val cardBackground: Color,
    val pillInactive: Color,
    val pillActive: Color,
    val gridListActive: Color,
    val softYellow: Color = SoftButterYellow,
    val softYellowSubtle: Color = SoftButterYellowSubtle,
    val softYellowBorder: Color = SoftButterYellowBorder,
    val primaryBlack: Color = PrimaryBlackText
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        border = SubtleBorderLight,
        amberAccent = SoftButterYellow,
        amberAccentSubtle = SoftButterYellowSubtle,
        warmError = SlateDestructive,
        textSecondary = CharcoalTextSecondary,
        cardBackground = CleanCardBackground,
        pillInactive = PillInactiveBackground,
        pillActive = PillActiveBackground,
        gridListActive = GridListActiveBackground,
        softYellow = SoftButterYellow,
        softYellowSubtle = SoftButterYellowSubtle,
        softYellowBorder = SoftButterYellowBorder,
        primaryBlack = PrimaryBlackText
    )
}

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlackText,
    onPrimary = Color.White,
    primaryContainer = SoftButterYellowSubtle,
    onPrimaryContainer = PrimaryBlackText,
    secondary = CharcoalTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = PillInactiveBackground,
    onSecondaryContainer = PrimaryBlackText,
    background = PureWhiteBackground,
    onBackground = PrimaryBlackText,
    surface = CleanCardBackground,
    onSurface = PrimaryBlackText,
    surfaceVariant = PillInactiveBackground,
    onSurfaceVariant = CharcoalTextSecondary,
    outline = SubtleBorderLight,
    outlineVariant = SubtleBorderLight,
    error = SlateDestructive,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmCaramelAccentDark,
    onPrimary = CharcoalBackgroundDark,
    primaryContainer = CharcoalSurfaceVariantDark,
    onPrimaryContainer = WarmCaramelAccentDark,
    secondary = WarmTextSecondaryDark,
    onSecondary = Color.White,
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
    error = SlateDestructiveDark,
    onError = Color.White
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
            amberAccent = WarmCaramelAccentDark,
            amberAccentSubtle = CharcoalSurfaceVariantDark,
            warmError = SlateDestructiveDark,
            textSecondary = WarmTextSecondaryDark,
            cardBackground = CharcoalSurfaceDark,
            pillInactive = CharcoalSurfaceVariantDark,
            pillActive = Color.White,
            gridListActive = CharcoalSurfaceVariantDark,
            softYellow = SoftButterYellow,
            softYellowSubtle = CharcoalSurfaceVariantDark,
            softYellowBorder = CharcoalBorderDark,
            primaryBlack = Color(0xFFEDEDED)
        )
    } else {
        ExtendedColors(
            border = SubtleBorderLight,
            amberAccent = SoftButterYellow,
            amberAccentSubtle = SoftButterYellowSubtle,
            warmError = SlateDestructive,
            textSecondary = CharcoalTextSecondary,
            cardBackground = CleanCardBackground,
            pillInactive = PillInactiveBackground,
            pillActive = PillActiveBackground,
            gridListActive = GridListActiveBackground,
            softYellow = SoftButterYellow,
            softYellowSubtle = SoftButterYellowSubtle,
            softYellowBorder = SoftButterYellowBorder,
            primaryBlack = PrimaryBlackText
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
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
