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
    val surfaceElevated: Color,
    val border: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val primaryIcons: Color,
    val secondaryIcons: Color,
    val interactiveAccent: Color,
    val pressedSurface: Color,
    val disabled: Color,
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
    // Backward-compatible bridges
    val softYellow: Color = interactiveAccent,
    val softYellowSubtle: Color = surfaceSecondary,
    val softYellowBorder: Color = border,
    val amberAccent: Color = interactiveAccent,
    val amberAccentSubtle: Color = surfaceSecondary
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        background = LightBackground,
        cardBackground = LightCardBackground,
        surfaceSecondary = LightSecondarySurface,
        surfaceElevated = LightElevatedSurface,
        border = LightBorder,
        primaryText = LightPrimaryText,
        secondaryText = LightSecondaryText,
        tertiaryText = LightTertiaryText,
        primaryIcons = LightPrimaryIcons,
        secondaryIcons = LightSecondaryIcons,
        interactiveAccent = LightInteractiveAccent,
        pressedSurface = LightPressedSurface,
        disabled = LightDisabled,
        textSecondary = LightSecondaryText,
        primaryBlack = LightPrimaryText,
        buttonPrimary = LightPrimaryText,
        buttonPrimaryText = Color.White,
        buttonSecondary = LightSecondarySurface,
        buttonSecondaryText = LightPrimaryText,
        pillActive = LightPrimaryText,
        pillActiveText = Color.White,
        pillInactive = LightSecondarySurface,
        pillInactiveText = LightPrimaryText,
        gridListActive = GridListActiveBackground,
        warmError = SlateDestructive,
        softYellow = LightInteractiveAccent,
        softYellowSubtle = LightSecondarySurface,
        softYellowBorder = LightBorder,
        amberAccent = LightInteractiveAccent,
        amberAccentSubtle = LightSecondarySurface
    )
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryText,
    onPrimary = Color.White,
    primaryContainer = LightSecondarySurface,
    onPrimaryContainer = LightPrimaryText,
    secondary = LightSecondaryText,
    onSecondary = Color.White,
    secondaryContainer = LightSecondarySurface,
    onSecondaryContainer = LightPrimaryText,
    background = LightBackground,
    onBackground = LightPrimaryText,
    surface = LightCardBackground,
    onSurface = LightPrimaryText,
    surfaceVariant = LightSecondarySurface,
    onSurfaceVariant = LightSecondaryText,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = SlateDestructive,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryText,
    onPrimary = LightPrimaryText,
    primaryContainer = DarkSecondarySurface,
    onPrimaryContainer = DarkPrimaryText,
    secondary = DarkSecondaryText,
    onSecondary = LightPrimaryText,
    secondaryContainer = DarkSecondarySurface,
    onSecondaryContainer = DarkPrimaryText,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkCardBackground,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkSecondarySurface,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
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
            background = DarkBackground,
            cardBackground = DarkCardBackground,
            surfaceSecondary = DarkSecondarySurface,
            surfaceElevated = DarkElevatedSurface,
            border = DarkBorder,
            primaryText = DarkPrimaryText,
            secondaryText = DarkSecondaryText,
            tertiaryText = DarkTertiaryText,
            primaryIcons = DarkPrimaryIcons,
            secondaryIcons = DarkSecondaryIcons,
            interactiveAccent = DarkInteractiveAccent,
            pressedSurface = DarkPressedSurface,
            disabled = DarkDisabled,
            textSecondary = DarkSecondaryText,
            primaryBlack = DarkPrimaryText,
            buttonPrimary = DarkPrimaryText,
            buttonPrimaryText = LightPrimaryText,
            buttonSecondary = DarkSecondarySurface,
            buttonSecondaryText = DarkPrimaryText,
            pillActive = DarkPrimaryText,
            pillActiveText = LightPrimaryText,
            pillInactive = DarkSecondarySurface,
            pillInactiveText = DarkPrimaryText,
            gridListActive = DarkSecondarySurface,
            warmError = SlateDestructiveDark,
            softYellow = DarkInteractiveAccent,
            softYellowSubtle = DarkSecondarySurface,
            softYellowBorder = DarkBorder,
            amberAccent = DarkInteractiveAccent,
            amberAccentSubtle = DarkSecondarySurface
        )
    } else {
        ExtendedColors(
            background = LightBackground,
            cardBackground = LightCardBackground,
            surfaceSecondary = LightSecondarySurface,
            surfaceElevated = LightElevatedSurface,
            border = LightBorder,
            primaryText = LightPrimaryText,
            secondaryText = LightSecondaryText,
            tertiaryText = LightTertiaryText,
            primaryIcons = LightPrimaryIcons,
            secondaryIcons = LightSecondaryIcons,
            interactiveAccent = LightInteractiveAccent,
            pressedSurface = LightPressedSurface,
            disabled = LightDisabled,
            textSecondary = LightSecondaryText,
            primaryBlack = LightPrimaryText,
            buttonPrimary = LightPrimaryText,
            buttonPrimaryText = Color.White,
            buttonSecondary = LightSecondarySurface,
            buttonSecondaryText = LightPrimaryText,
            pillActive = LightPrimaryText,
            pillActiveText = Color.White,
            pillInactive = LightSecondarySurface,
            pillInactiveText = LightPrimaryText,
            gridListActive = GridListActiveBackground,
            warmError = SlateDestructive,
            softYellow = LightInteractiveAccent,
            softYellowSubtle = LightSecondarySurface,
            softYellowBorder = LightBorder,
            amberAccent = LightInteractiveAccent,
            amberAccentSubtle = LightSecondarySurface
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
