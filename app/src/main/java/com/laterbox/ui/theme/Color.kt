package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Exact Palette — Calibrated directly from reference screenshots
// (uploaded_media_0, uploaded_media_1, uploaded_media_2, uploaded_media_3)
// Strictly NO RED FONT anywhere in the app!

val WarmCreamBackground = Color(0xFFF4F0E8)       // Exact main screen backdrop (#F4F0E8)
val WarmCardBackground = Color(0xFFFBF8F3)        // Exact card surface & bottom bar (#FBF8F3)
val WarmBorderDashed = Color(0xFFE2D9CC)          // Exact dashed empty card border (#E2D9CC)
val WarmBorderLight = Color(0xFFE2D9CC)           // Exact subtle border & input stroke (#E2D9CC)

val WarmCaramelAccent = Color(0xFF9C6E1E)         // Exact golden amber/caramel button (#9C6E1E)
val WarmCaramelAccentLight = Color(0xFFAE7C24)    // Slightly lighter caramel for hover/focus
val WarmCaramelSubtle = Color(0xFFFDF6EA)         // Soft warm container tint

val WarmTextPrimaryLight = Color(0xFF1C1915)      // Exact dark typography (#1C1915)
val WarmTextSecondaryLight = Color(0xFF5C564E)    // Exact muted secondary typography (#5C564E)

val PillInactiveBackground = Color(0xFFEFE8DC)    // Exact inactive filter chips (#EFE8DC)
val PillActiveBackground = Color(0xFF1C1915)      // Exact active filter chip "All" (#1C1915)
val GridListActiveBackground = Color(0xFFF3E4C4)  // Exact active toggle pill "Grid" (#F3E4C4)
val ScrollThumbColor = Color(0xFF716C64)          // Exact horizontal scroll indicator thumb (#716C64)

// Compatibility aliases
val AmberAccent = WarmCaramelAccent
val AmberAccentLight = WarmCaramelAccentLight
val PureWhiteBackground = WarmCreamBackground
val CleanCardBackground = WarmCardBackground
val CleanCardElevated = WarmCardBackground
val SubtleBorderLight = WarmBorderLight
val SubtleBorderStrong = WarmBorderLight
val VibrantYellowAccent = WarmCaramelAccent
val VibrantYellowAccentLight = WarmCaramelAccentLight
val VibrantYellowSubtle = WarmCaramelSubtle
val SolidBlackText = WarmTextPrimaryLight
val CharcoalTextSecondary = WarmTextSecondaryLight
val MutedTextTertiary = WarmTextSecondaryLight

// Dark Mode Adaptive Palette — Warm charcoal
val CharcoalBackgroundDark = Color(0xFF161412)
val CharcoalSurfaceDark = Color(0xFF1E1B18)
val CharcoalSurfaceVariantDark = Color(0xFF282522)
val WarmTextPrimaryDark = Color(0xFFF4F0E8)
val WarmTextSecondaryDark = Color(0xFF9E978D)
val CharcoalBorderDark = Color(0xFF2C2723)
val WarmCaramelAccentDark = Color(0xFFB38029)

val DeepBlackBackgroundDark = CharcoalBackgroundDark
val WhiteTextPrimaryDark = WarmTextPrimaryDark
val NeutralTextSecondaryDark = WarmTextSecondaryDark
val VibrantYellowAccentDark = WarmCaramelAccentDark

// Destructive Actions — Strictly NO RED FONT
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
