package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Exact Palette — Sampled directly from user's reference screenshots:
// uploaded_media_0_1794250924023.png & uploaded_media_2_1794250924023.png
val WarmCreamBackground = Color(0xFFF4F0E8)       // Exact main background (51.3% pixels)
val WarmCardBackground = Color(0xFFFBF8F3)        // Exact card & bottom bar background (35% pixels)
val WarmBorderDashed = Color(0xFFE2D9CC)          // Exact dashed empty state border & outlines (0.7% pixels)
val WarmBorderLight = Color(0xFFE2D9CC)           // Exact subtle separator & input border
val WarmCaramelAccent = Color(0xFF9C6E1E)         // Exact ochre/caramel gold button & badge accent (2.65% pixels)
val WarmCaramelAccentLight = Color(0xFFAE7C24)    // Slightly lighter caramel for hover/focus states
val WarmCaramelSubtle = Color(0xFFFDF6EA)         // Soft warm amber tint for subtle highlights
val PillInactiveBackground = Color(0xFFEFE8DC)    // Exact inactive filter chips (Inbox, Videos, etc.) (2.39% pixels)
val PillActiveBackground = Color(0xFF1C1915)      // Exact active filter chip ("All") & dark active state (0.87% pixels)
val GridListActiveBackground = Color(0xFFF3E4C4)  // Exact active toggle pill ("Grid") inside toggle control (0.54% pixels)
val ScrollThumbColor = Color(0xFF716C64)          // Exact horizontal scroll indicator thumb color

val WarmTextPrimaryLight = Color(0xFF1C1915)      // Exact dark typography for headings & active tabs (0.87% pixels)
val WarmTextSecondaryLight = Color(0xFF5C564E)    // Exact muted secondary typography for subtitles & inactive tabs (0.16% pixels)

// Aliases for backward-compatibility
val AmberAccent = WarmCaramelAccent
val AmberAccentLight = WarmCaramelAccentLight

// Dark Mode Adaptive Palette — Refined charcoal with warm neutral undertones
val CharcoalBackgroundDark = Color(0xFF161412)
val CharcoalSurfaceDark = Color(0xFF1E1B18)
val CharcoalSurfaceVariantDark = Color(0xFF282522)
val WarmTextPrimaryDark = Color(0xFFF4F0E8)
val WarmTextSecondaryDark = Color(0xFF9E978D)
val CharcoalBorderDark = Color(0xFF2C2723)
val WarmCaramelAccentDark = Color(0xFFB38029)

// Destructive Actions — Earthy terracotta (Zero Crimson)
val WarmTerracotta = Color(0xFF9E3E2F)
val WarmTerracottaDark = Color(0xFFCF5E4D)
