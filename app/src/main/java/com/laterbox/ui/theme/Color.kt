package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Global Palette: White Background, Black Typography, Vibrant Yellow / Golden Accent
// Strictly NO RED FONT anywhere in the app!

// Light Mode: Pure White Background, Solid Black, Vibrant Yellow Accent
val PureWhiteBackground = Color(0xFFFFFFFF)       // Global White BG
val CleanCardBackground = Color(0xFFFFFFFF)       // Crisp Card surface
val CleanCardElevated = Color(0xFFF9FAFB)         // Slight surface variation
val SubtleBorderLight = Color(0xFFE5E7EB)         // Clean light neutral border
val SubtleBorderStrong = Color(0xFFD1D5DB)        // Input and toggle border

val VibrantYellowAccent = Color(0xFFF5BA13)       // Primary Yellow / Gold accent (#F5BA13)
val VibrantYellowAccentLight = Color(0xFFFDE047)  // Secondary yellow tint
val VibrantYellowSubtle = Color(0xFFFEF9C3)       // Soft yellow container tint

val SolidBlackText = Color(0xFF000000)            // Deep Black primary text
val CharcoalTextSecondary = Color(0xFF4B5563)     // Neutral Charcoal secondary text
val MutedTextTertiary = Color(0xFF6B7280)         // Tertiary metadata

val PillInactiveBackground = Color(0xFFF3F4F6)    // Clean light grey inactive pill
val PillActiveBackground = Color(0xFF000000)      // Solid black active pill
val GridListActiveBackground = Color(0xFFFEF08A)  // Soft yellow active toggle pill

// Aliases for compatibility
val WarmCreamBackground = PureWhiteBackground
val WarmCardBackground = CleanCardBackground
val WarmBorderDashed = SubtleBorderLight
val WarmBorderLight = SubtleBorderLight
val WarmCaramelAccent = VibrantYellowAccent
val WarmCaramelAccentLight = VibrantYellowAccentLight
val WarmCaramelSubtle = VibrantYellowSubtle
val WarmTextPrimaryLight = SolidBlackText
val WarmTextSecondaryLight = CharcoalTextSecondary
val AmberAccent = VibrantYellowAccent
val AmberAccentLight = VibrantYellowAccentLight
val ScrollThumbColor = Color(0xFF4B5563)

// Dark Mode: Deep Black, Crisp White Text, Golden Yellow Accent
val DeepBlackBackgroundDark = Color(0xFF09090B)
val CharcoalSurfaceDark = Color(0xFF141417)
val CharcoalSurfaceVariantDark = Color(0xFF202024)
val WhiteTextPrimaryDark = Color(0xFFFFFFFF)
val NeutralTextSecondaryDark = Color(0xFF9CA3AF)
val CharcoalBorderDark = Color(0xFF27272A)
val VibrantYellowAccentDark = Color(0xFFFACC15)

val CharcoalBackgroundDark = DeepBlackBackgroundDark
val WarmTextPrimaryDark = WhiteTextPrimaryDark
val WarmTextSecondaryDark = NeutralTextSecondaryDark
val WarmCaramelAccentDark = VibrantYellowAccentDark

// Non-red destructive & warning colors (Strictly NO RED FONT)
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
