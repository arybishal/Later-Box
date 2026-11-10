package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Later Box v2.0.0 Design System Palette
// Aesthetic: Soft. Minimal. Premium. Spacious. Modern. Content-focused.
// =============================================================================

// --- Light Theme Tokens ---
val LightBackground = Color(0xFFF0F1F3)           // Main background (#F0F1F3)
val LightCardBackground = Color(0xFFFFFFFF)       // Primary card background (#FFFFFF)
val LightSecondarySurface = Color(0xFFF5F5F5)     // Secondary surface (#F5F5F5)
val LightElevatedSurface = Color(0xFFFFFFFF)      // Elevated surface (#FFFFFF)
val LightPrimaryText = Color(0xFF171717)          // Primary text (#171717)
val LightSecondaryText = Color(0xFF777777)        // Secondary text (#777777)
val LightTertiaryText = Color(0xFF999999)         // Tertiary text (#999999)
val LightPrimaryIcons = Color(0xFF171717)         // Primary icons (#171717)
val LightSecondaryIcons = Color(0xFF8B8B8B)       // Secondary icons (#8B8B8B)
val LightBorder = Color(0xFFE9E9E9)               // Borders and dividers (#E9E9E9)
val LightInteractiveAccent = Color(0xFF1687D9)    // Primary interactive accent blue (#1687D9)
val LightPressedSurface = Color(0xFFE7E8EA)       // Pressed neutral surface (#E7E8EA)
val LightDisabled = Color(0xFFB5B5B5)             // Disabled content (#B5B5B5)

// Backward-compatibility aliases for Light theme
val PureWhiteBackground = LightBackground         // Points to main background (#F0F1F3)
val CleanCardBackground = LightCardBackground     // Points to white card (#FFFFFF)
val NeutralSurfaceLight = LightSecondarySurface   // Points to secondary surface (#F5F5F5)
val SubtleBorderLight = LightBorder               // Points to soft border (#E9E9E9)
val SubtleBorderStrong = Color(0xFFD1D5DB)        // Input & toggle stroke (#D1D5DB)
val PrimaryBlackText = LightPrimaryText           // Points to primary text (#171717)
val SolidBlackText = LightPrimaryText
val NeutralGreySecondary = LightSecondaryText     // Points to secondary text (#777777)
val CharcoalTextSecondary = LightSecondaryText
val MutedTextTertiary = LightTertiaryText         // Points to tertiary text (#999999)
val DisabledNeutralLight = LightDisabled          // Points to disabled (#B5B5B5)

val PillInactiveBackground = LightSecondarySurface
val PillActiveBackground = LightPrimaryText
val GridListActiveBackground = LightPrimaryText
val ScrollThumbColor = LightSecondaryText
val PrimaryButtonLight = LightPrimaryText
val PrimaryButtonTextLight = Color(0xFFFFFFFF)

// --- Dark Theme Tokens ---
val DarkBackground = Color(0xFF101113)            // Main background (#101113)
val DarkCardBackground = Color(0xFF1C1D20)        // Primary card background (#1C1D20)
val DarkSecondarySurface = Color(0xFF252629)      // Secondary surface (#252629)
val DarkElevatedSurface = Color(0xFF292A2E)       // Elevated surface (#292A2E)
val DarkPrimaryText = Color(0xFFFFFFFF)           // Primary text (#FFFFFF)
val DarkSecondaryText = Color(0xFFB7B7BB)         // Secondary text (#B7B7BB)
val DarkTertiaryText = Color(0xFF88888E)          // Tertiary text (#88888E)
val DarkPrimaryIcons = Color(0xFFFFFFFF)          // Primary icons (#FFFFFF)
val DarkSecondaryIcons = Color(0xFFA0A0A5)        // Secondary icons (#A0A0A5)
val DarkBorder = Color(0xFF333438)                // Borders and dividers (#333438)
val DarkInteractiveAccent = Color(0xFF55A9EE)     // Interactive accent blue (#55A9EE)
val DarkPressedSurface = Color(0xFF303136)        // Pressed neutral surface (#303136)
val DarkDisabled = Color(0xFF68686D)              // Disabled content (#68686D)

// Backward-compatibility aliases for Dark theme
val CharcoalBackgroundDark = DarkBackground
val CharcoalSurfaceDark = DarkCardBackground
val CharcoalSurfaceVariantDark = DarkSecondarySurface
val WarmTextPrimaryDark = DarkPrimaryText
val WarmTextSecondaryDark = DarkSecondaryText
val CharcoalBorderDark = DarkBorder
val DisabledNeutralDark = DarkDisabled
val DeepBlackBackgroundDark = DarkBackground
val WhiteTextPrimaryDark = DarkPrimaryText
val NeutralTextSecondaryDark = DarkSecondaryText
val PrimaryButtonDark = DarkPrimaryText
val PrimaryButtonTextDark = LightPrimaryText

// Destructive Actions — Strictly Slate/Charcoal (NO RED FONT)
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
