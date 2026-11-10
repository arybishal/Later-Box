package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Global Palette: Clean White Background, Solid Black Typography, Vibrant Yellow Accent
// Strictly NO BROWN / OCHRE (#9E6F21 / #9C6E1E) and strictly NO RED FONT anywhere!

// Light Mode: Pure White Background, Solid Black Text, Vibrant Yellow Accent
val PureWhiteBackground = Color(0xFFFFFFFF)       // Crisp White Main Background (#FFFFFF)
val CleanCardBackground = Color(0xFFFFFFFF)       // Crisp White Card & Bottom Bar (#FFFFFF)
val CleanCardElevated = Color(0xFFF9FAFB)         // Subtle elevated surface (#F9FAFB)
val SubtleBorderLight = Color(0xFFE5E7EB)         // Clean light neutral border (#E5E7EB)
val SubtleBorderStrong = Color(0xFFD1D5DB)        // Input & toggle stroke (#D1D5DB)

val VibrantYellowAccent = Color(0xFFF5BA13)       // Clean Vibrant Yellow (#F5BA13)
val VibrantYellowAccentLight = Color(0xFFFDE047)  // Bright secondary yellow tint (#FDE047)
val VibrantYellowSubtle = Color(0xFFFEF9C3)       // Soft yellow container tint (#FEF9C3)

val SolidBlackText = Color(0xFF000000)            // Deep Black primary text (#000000)
val CharcoalTextSecondary = Color(0xFF4B5563)     // Neutral Charcoal secondary text (#4B5563)
val MutedTextTertiary = Color(0xFF6B7280)         // Tertiary metadata (#6B7280)

val PillInactiveBackground = Color(0xFFF3F4F6)    // Clean light grey inactive pill (#F3F4F6)
val PillActiveBackground = Color(0xFF000000)      // Solid black active pill (#000000)
val GridListActiveBackground = Color(0xFFFEF08A)  // Soft yellow active toggle pill (#FEF08A)
val ScrollThumbColor = Color(0xFF4B5563)          // Clean neutral scroll thumb (#4B5563)

// Universal Theme Aliases
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

// Dark Mode Adaptive Palette — Deep Charcoal / Black with Golden Yellow
val CharcoalBackgroundDark = Color(0xFF09090B)
val CharcoalSurfaceDark = Color(0xFF141417)
val CharcoalSurfaceVariantDark = Color(0xFF202024)
val WarmTextPrimaryDark = Color(0xFFFFFFFF)
val WarmTextSecondaryDark = Color(0xFF9CA3AF)
val CharcoalBorderDark = Color(0xFF27272A)
val WarmCaramelAccentDark = Color(0xFFFACC15)

val DeepBlackBackgroundDark = CharcoalBackgroundDark
val WhiteTextPrimaryDark = WarmTextPrimaryDark
val NeutralTextSecondaryDark = WarmTextSecondaryDark
val VibrantYellowAccentDark = WarmCaramelAccentDark

// Destructive Actions — Strictly NO RED FONT
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
