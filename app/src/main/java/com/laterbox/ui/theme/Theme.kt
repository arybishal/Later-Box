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
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.laterbox.data.model.ThemeMode

@Immutable
data class ExtendedColors(
    val background: Color,
    val cardBackground: Color,
    val surfaceSecondary: Color,
    val border: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val textSecondary: Color = secondaryText,
    val primaryBlack: Color = primaryText,
    val buttonPrimary: Color,
    val buttonPrimaryText: Color,
    val buttonSecondary: Color,
    val buttonSecondaryText: Color,
    val pillActive: Color,
    val pillActiveText: Color,
    val pillInactive: Color,
    val pillInactiveText: Color,
    val gridListActive: Color,
    val warmError: Color,
    val disabled: Color,
    // Strictly monochrome backward-compatible bridges (ZERO YELLOW)
    val softYellow: Color = buttonPrimary,
    val softYellowSubtle: Color = surfaceSecondary,
    val softYellowBorder: Color = border,
    val amberAccent: Color = primaryText,
    val amberAccentSubtle: Color = surfaceSecondary
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        background = PureWhiteBackground,
        cardBackground = CleanCardBackground,
        surfaceSecondary = NeutralSurfaceLight,
        border = SubtleBorderLight,
        primaryText = PrimaryBlackText,
        secondaryText = NeutralGreySecondary,
        textSecondary = NeutralGreySecondary,
        primaryBlack = PrimaryBlackText,
        buttonPrimary = PrimaryBlackText,
        buttonPrimaryText = Color.White,
        buttonSecondary = NeutralSurfaceLight,
        buttonSecondaryText = PrimaryBlackText,
        pillActive = PrimaryBlackText,
        pillActiveText = Color.White,
        pillInactive = NeutralSurfaceLight,
        pillInactiveText = PrimaryBlackText,
        gridListActive = GridListActiveBackground,
        warmError = SlateDestructive,
        disabled = DisabledNeutralLight,
        softYellow = PrimaryBlackText,
        softYellowSubtle = NeutralSurfaceLight,
        softYellowBorder = SubtleBorderLight,
        amberAccent = PrimaryBlackText,
        amberAccentSubtle = NeutralSurfaceLight
    )
}

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlackText,
    onPrimary = Color.White,
    primaryContainer = NeutralSurfaceLight,
    onPrimaryContainer = PrimaryBlackText,
    secondary = NeutralGreySecondary,
    onSecondary = Color.White,
    secondaryContainer = NeutralSurfaceLight,
    onSecondaryContainer = PrimaryBlackText,
    background = PureWhiteBackground,
    onBackground = PrimaryBlackText,
    surface = CleanCardBackground,
    onSurface = PrimaryBlackText,
    surfaceVariant = NeutralSurfaceLight,
    onSurfaceVariant = NeutralGreySecondary,
    outline = SubtleBorderLight,
    outlineVariant = SubtleBorderLight,
    error = SlateDestructive,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = PrimaryBlackText,
    primaryContainer = CharcoalSurfaceVariantDark,
    onPrimaryContainer = Color.White,
    secondary = WarmTextSecondaryDark,
    onSecondary = PrimaryBlackText,
    secondaryContainer = CharcoalSurfaceVariantDark,
    onSecondaryContainer = Color.White,
    background = CharcoalBackgroundDark,
    onBackground = Color.White,
    surface = CharcoalSurfaceDark,
    onSurface = Color.White,
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
            background = CharcoalBackgroundDark,
            cardBackground = CharcoalSurfaceDark,
            surfaceSecondary = CharcoalSurfaceVariantDark,
            border = CharcoalBorderDark,
            primaryText = Color.White,
            secondaryText = WarmTextSecondaryDark,
            textSecondary = WarmTextSecondaryDark,
            primaryBlack = Color.White,
            buttonPrimary = Color.White,
            buttonPrimaryText = PrimaryBlackText,
            buttonSecondary = CharcoalSurfaceVariantDark,
            buttonSecondaryText = Color.White,
            pillActive = Color.White,
            pillActiveText = PrimaryBlackText,
            pillInactive = CharcoalSurfaceVariantDark,
            pillInactiveText = Color.White,
            gridListActive = CharcoalSurfaceVariantDark,
            warmError = SlateDestructiveDark,
            disabled = DisabledNeutralDark,
            softYellow = Color.White,
            softYellowSubtle = CharcoalSurfaceVariantDark,
            softYellowBorder = CharcoalBorderDark,
            amberAccent = Color.White,
            amberAccentSubtle = CharcoalSurfaceVariantDark
        )
    } else {
        ExtendedColors(
            background = PureWhiteBackground,
            cardBackground = CleanCardBackground,
            surfaceSecondary = NeutralSurfaceLight,
            border = SubtleBorderLight,
            primaryText = PrimaryBlackText,
            secondaryText = NeutralGreySecondary,
            textSecondary = NeutralGreySecondary,
            primaryBlack = PrimaryBlackText,
            buttonPrimary = PrimaryBlackText,
            buttonPrimaryText = Color.White,
            buttonSecondary = NeutralSurfaceLight,
            buttonSecondaryText = PrimaryBlackText,
            pillActive = PrimaryBlackText,
            pillActiveText = Color.White,
            pillInactive = NeutralSurfaceLight,
            pillInactiveText = PrimaryBlackText,
            gridListActive = GridListActiveBackground,
            warmError = SlateDestructive,
            disabled = DisabledNeutralLight,
            softYellow = PrimaryBlackText,
            softYellowSubtle = NeutralSurfaceLight,
            softYellowBorder = SubtleBorderLight,
            amberAccent = PrimaryBlackText,
            amberAccentSubtle = NeutralSurfaceLight
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
