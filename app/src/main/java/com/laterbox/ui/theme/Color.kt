package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Global Palette: Pure White (#FFFFFF), Deep Black (#111111), and Soft Butter Yellow (#FFF1A6)
// Strictly ZERO brown/ochre (#9E6F21 / #9C6E1E) and strictly ZERO red font anywhere!

// Light Mode: Pure White Background, Solid Black Typography, Soft Yellow Accent
val PureWhiteBackground = Color(0xFFFFFFFF)       // Pure White Main Background (#FFFFFF)
val CleanCardBackground = Color(0xFFFFFFFF)       // Pure White Card & Surface (#FFFFFF)
val CleanCardElevated = Color(0xFFFAFAFA)         // Subtle elevated surface (#FAFAFA)
val SubtleBorderLight = Color(0xFFE5E7EB)         // Clean light neutral border (#E5E7EB)
val SubtleBorderStrong = Color(0xFFD1D5DB)        // Input & toggle stroke (#D1D5DB)

val SoftButterYellow = Color(0xFFFFF1A6)          // Consistent Soft Butter Yellow (#FFF1A6)
val SoftButterYellowSubtle = Color(0xFFFFFBEA)    // Soft yellow container tint (#FFFBEA)
val SoftButterYellowBorder = Color(0xFFFDE68A)    // Delicate yellow border highlight (#FDE68A)

val PrimaryBlackText = Color(0xFF111111)          // Primary Black Typography & Headings (#111111)
val SolidBlackText = PrimaryBlackText             // Alias for high-contrast primary text
val CharcoalTextSecondary = Color(0xFF6B7280)     // Neutral Grey secondary text (#6B7280)
val MutedTextTertiary = Color(0xFF9CA3AF)         // Neutral Grey tertiary metadata (#9CA3AF)

val PillInactiveBackground = Color(0xFFF4F4F5)    // Neutral light grey inactive pill (#F4F4F5)
val PillActiveBackground = PrimaryBlackText       // Solid black active pill (#111111)
val GridListActiveBackground = SoftButterYellow   // Soft yellow active toggle pill (#FFF1A6)
val ScrollThumbColor = Color(0xFF6B7280)          // Neutral grey scroll thumb (#6B7280)

// Universal Theme Compatibility Aliases
val VibrantYellowAccent = SoftButterYellow
val VibrantYellowAccentLight = Color(0xFFFFF4B8)
val VibrantYellowSubtle = SoftButterYellowSubtle
val WarmCreamBackground = PureWhiteBackground
val WarmCardBackground = CleanCardBackground
val WarmBorderDashed = SubtleBorderLight
val WarmBorderLight = SubtleBorderLight
val WarmCaramelAccent = SoftButterYellow
val WarmCaramelAccentLight = Color(0xFFFFF4B8)
val WarmCaramelSubtle = SoftButterYellowSubtle
val WarmTextPrimaryLight = PrimaryBlackText
val WarmTextSecondaryLight = CharcoalTextSecondary
val AmberAccent = SoftButterYellow
val AmberAccentLight = Color(0xFFFFF4B8)

// Dark Mode Adaptive Palette — Deep Charcoal / Black with Soft Butter Yellow
val CharcoalBackgroundDark = Color(0xFF0D0D0E)
val CharcoalSurfaceDark = Color(0xFF17171A)
val CharcoalSurfaceVariantDark = Color(0xFF222226)
val WarmTextPrimaryDark = Color(0xFFEDEDED)
val WarmTextSecondaryDark = Color(0xFFA1A1AA)
val CharcoalBorderDark = Color(0xFF27272A)
val WarmCaramelAccentDark = Color(0xFFFFF1A6)

val DeepBlackBackgroundDark = CharcoalBackgroundDark
val WhiteTextPrimaryDark = WarmTextPrimaryDark
val NeutralTextSecondaryDark = WarmTextSecondaryDark
val VibrantYellowAccentDark = WarmCaramelAccentDark

// Destructive Actions — Strictly NO RED FONT
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
