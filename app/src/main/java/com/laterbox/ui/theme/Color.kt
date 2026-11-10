package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Global Palette: Pure White (#FFFFFF), Solid Typography (#111111) & Soft Butter Yellow (#FFF4B8)
// Minimalist Premium Aesthetic — ZERO #9E6F21, ZERO Brown/Ochre, ZERO Red font!

// Light Mode: Pure White Background, High-Contrast Typography, Soft Butter Yellow Accents
val PureWhiteBackground = Color(0xFFFFFFFF)       // Pure White Background (#FFFFFF)
val CleanCardBackground = Color(0xFFFFFFFF)       // Pure White Card & Surface (#FFFFFF)
val CleanCardElevated = Color(0xFFFAFAFA)         // Subtle elevated surface (#FAFAFA)
val SubtleBorderLight = Color(0xFFE5E7EB)         // Clean light neutral border (#E5E7EB)
val SubtleBorderStrong = Color(0xFFD1D5DB)        // Input & toggle stroke (#D1D5DB)

val PrimaryBlackText = Color(0xFF111111)          // Primary Typography & Headings (#111111)
val SolidBlackText = PrimaryBlackText             // High-contrast primary text
val CharcoalTextSecondary = Color(0xFF555555)     // Neutral Grey secondary text (#555555)
val MutedTextTertiary = Color(0xFF888888)         // Neutral Grey tertiary metadata (#888888)

val PillInactiveBackground = Color(0xFFF4F4F5)    // Neutral light grey inactive pill (#F4F4F5)
val PillActiveBackground = PrimaryBlackText       // Active pill (#111111)
val GridListActiveBackground = Color(0xFF111111)  // Active toggle pill (#111111)
val ScrollThumbColor = Color(0xFF555555)          // Neutral grey scroll thumb (#555555)

// Soft Butter Yellow (#FFF4B8) for Primary Actions, Badges & Focal Highlights
val SoftButterYellow = Color(0xFFFFF4B8)          // Soft Butter Yellow (#FFF4B8)
val SoftButterYellowSubtle = Color(0xFFFFFBEA)    // Very light butter yellow surface (#FFFBEA)
val SoftButterYellowBorder = Color(0xFFF9EEB8)    // Soft yellow border (#F9EEB8)
val VibrantYellowAccent = SoftButterYellow
val VibrantYellowAccentLight = SoftButterYellowSubtle
val VibrantYellowSubtle = SoftButterYellowSubtle
val WarmCreamBackground = PureWhiteBackground
val WarmCardBackground = CleanCardBackground
val WarmBorderDashed = SubtleBorderLight
val WarmBorderLight = SubtleBorderLight
val WarmCaramelAccent = SoftButterYellow
val WarmCaramelAccentLight = SoftButterYellowSubtle
val WarmCaramelSubtle = SoftButterYellowSubtle
val WarmTextPrimaryLight = PrimaryBlackText
val WarmTextSecondaryLight = CharcoalTextSecondary
val AmberAccent = SoftButterYellow
val AmberAccentLight = SoftButterYellowSubtle
val AmberAccentSubtle = SoftButterYellowSubtle

// Dark Mode Adaptive Palette — Deep Black (#000000) & Restrained Soft Yellow (#FFF4B8)
val CharcoalBackgroundDark = Color(0xFF000000)
val CharcoalSurfaceDark = Color(0xFF121212)
val CharcoalSurfaceVariantDark = Color(0xFF1E1E1E)
val WarmTextPrimaryDark = Color(0xFFFFFFFF)
val WarmTextSecondaryDark = Color(0xFFAAAAAA)
val CharcoalBorderDark = Color(0xFF2E2E2E)
val WarmCaramelAccentDark = Color(0xFFFFF4B8)      // Restrained soft yellow accent in dark mode

val DeepBlackBackgroundDark = CharcoalBackgroundDark
val WhiteTextPrimaryDark = WarmTextPrimaryDark
val NeutralTextSecondaryDark = WarmTextSecondaryDark
val VibrantYellowAccentDark = WarmCaramelAccentDark

// Destructive Actions — Strictly NO RED FONT
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
