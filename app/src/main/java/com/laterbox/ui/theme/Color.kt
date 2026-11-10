package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Global Palette: Pure White (#FFFFFF) & Solid Black (#000000)
// Minimalist Monochrome Aesthetic — ZERO Yellow, ZERO Amber, ZERO Brown, ZERO Red font!

// Light Mode: Pure White Background, Solid Black Typography, Monochrome Accents
val PureWhiteBackground = Color(0xFFFFFFFF)       // Pure White Background (#FFFFFF)
val CleanCardBackground = Color(0xFFFFFFFF)       // Pure White Card & Surface (#FFFFFF)
val CleanCardElevated = Color(0xFFFAFAFA)         // Subtle elevated surface (#FAFAFA)
val SubtleBorderLight = Color(0xFFE5E7EB)         // Clean light neutral border (#E5E7EB)
val SubtleBorderStrong = Color(0xFFD1D5DB)        // Input & toggle stroke (#D1D5DB)

val PrimaryBlackText = Color(0xFF000000)          // Pure Black Typography, Headings & Primary Buttons (#000000)
val SolidBlackText = PrimaryBlackText             // High-contrast primary text
val CharcoalTextSecondary = Color(0xFF555555)     // Neutral Grey secondary text (#555555)
val MutedTextTertiary = Color(0xFF888888)         // Neutral Grey tertiary metadata (#888888)

val PillInactiveBackground = Color(0xFFF4F4F5)    // Neutral light grey inactive pill (#F4F4F5)
val PillActiveBackground = PrimaryBlackText       // Solid black active pill (#000000)
val GridListActiveBackground = Color(0xFF000000)  // Solid black active toggle pill (#000000)
val ScrollThumbColor = Color(0xFF555555)          // Neutral grey scroll thumb (#555555)

// Universal Monochrome Aliases (replacing all yellow/amber with Black & Neutral)
val SoftButterYellow = PrimaryBlackText
val SoftButterYellowSubtle = PillInactiveBackground
val SoftButterYellowBorder = SubtleBorderLight
val VibrantYellowAccent = PrimaryBlackText
val VibrantYellowAccentLight = PillInactiveBackground
val VibrantYellowSubtle = PillInactiveBackground
val WarmCreamBackground = PureWhiteBackground
val WarmCardBackground = CleanCardBackground
val WarmBorderDashed = SubtleBorderLight
val WarmBorderLight = SubtleBorderLight
val WarmCaramelAccent = PrimaryBlackText
val WarmCaramelAccentLight = PillInactiveBackground
val WarmCaramelSubtle = PillInactiveBackground
val WarmTextPrimaryLight = PrimaryBlackText
val WarmTextSecondaryLight = CharcoalTextSecondary
val AmberAccent = PrimaryBlackText
val AmberAccentLight = PillInactiveBackground
val AmberAccentSubtle = PillInactiveBackground

// Dark Mode Adaptive Palette — Deep Black (#000000) & Crisp White (#FFFFFF)
val CharcoalBackgroundDark = Color(0xFF000000)
val CharcoalSurfaceDark = Color(0xFF121212)
val CharcoalSurfaceVariantDark = Color(0xFF1E1E1E)
val WarmTextPrimaryDark = Color(0xFFFFFFFF)
val WarmTextSecondaryDark = Color(0xFFAAAAAA)
val CharcoalBorderDark = Color(0xFF2E2E2E)
val WarmCaramelAccentDark = Color(0xFFFFFFFF)

val DeepBlackBackgroundDark = CharcoalBackgroundDark
val WhiteTextPrimaryDark = WarmTextPrimaryDark
val NeutralTextSecondaryDark = WarmTextSecondaryDark
val VibrantYellowAccentDark = WarmCaramelAccentDark

// Destructive Actions — Strictly NO RED FONT
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
