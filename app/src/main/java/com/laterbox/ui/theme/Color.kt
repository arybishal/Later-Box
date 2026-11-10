package com.laterbox.ui.theme

import androidx.compose.ui.graphics.Color

// Global Palette: Pure White (#FFFFFF), High-Contrast Typography (#111111) & Deep Black (#000000)
// Minimalist Premium Monochrome Aesthetic — Strictly ZERO yellow, ZERO amber, ZERO red font!

// Light Mode: Pure White Canvas, High-Contrast Typography, Neutral Grey Secondary
val PureWhiteBackground = Color(0xFFFFFFFF)       // Main background (#FFFFFF)
val CleanCardBackground = Color(0xFFFFFFFF)       // Card & Surface (#FFFFFF)
val NeutralSurfaceLight = Color(0xFFF2F2F2)       // Secondary surface (#F2F2F2)
val SubtleBorderLight = Color(0xFFE5E5E5)         // Borders and dividers (#E5E5E5)
val SubtleBorderStrong = Color(0xFFD1D5DB)        // Input & toggle stroke (#D1D5DB)

val PrimaryBlackText = Color(0xFF111111)          // Primary Typography, Icons & Primary Button (#111111)
val SolidBlackText = PrimaryBlackText             // High-contrast primary text (#111111)
val NeutralGreySecondary = Color(0xFF666666)      // Neutral Grey secondary text (#666666)
val CharcoalTextSecondary = NeutralGreySecondary  // Secondary text alias (#666666)
val MutedTextTertiary = Color(0xFF888888)         // Neutral Grey tertiary metadata (#888888)
val DisabledNeutralLight = Color(0xFFA0A0A0)      // Disabled elements in light mode (#A0A0A0)

val PillInactiveBackground = Color(0xFFF2F2F2)    // Neutral light grey inactive pill (#F2F2F2)
val PillActiveBackground = PrimaryBlackText       // Active pill (#111111)
val GridListActiveBackground = Color(0xFF111111)  // Active toggle pill (#111111)
val ScrollThumbColor = Color(0xFF666666)          // Neutral grey scroll thumb (#666666)

// Dark Mode Adaptive Palette — Deep Black (#000000), Pure White (#FFFFFF) & Neutral Greys
val CharcoalBackgroundDark = Color(0xFF000000)    // Main background (#000000)
val CharcoalSurfaceDark = Color(0xFF171717)       // Primary surface (#171717)
val CharcoalSurfaceVariantDark = Color(0xFF222222)// Secondary surface (#222222)
val WarmTextPrimaryDark = Color(0xFFFFFFFF)       // Primary text, Icons & Primary Button (#FFFFFF)
val WarmTextSecondaryDark = Color(0xFFB3B3B3)     // Secondary text (#B3B3B3)
val CharcoalBorderDark = Color(0xFF303030)        // Borders and dividers (#303030)
val DisabledNeutralDark = Color(0xFF666666)       // Disabled elements in dark mode (#666666)

val DeepBlackBackgroundDark = CharcoalBackgroundDark
val WhiteTextPrimaryDark = WarmTextPrimaryDark
val NeutralTextSecondaryDark = WarmTextSecondaryDark

// Primary Actions & Buttons (Monochrome: Black in Light, White in Dark)
val PrimaryButtonLight = PrimaryBlackText         // Primary button background light (#111111)
val PrimaryButtonTextLight = Color(0xFFFFFFFF)    // Primary button text light (#FFFFFF)
val PrimaryButtonDark = Color(0xFFFFFFFF)         // Primary button background dark (#FFFFFF)
val PrimaryButtonTextDark = PrimaryBlackText      // Primary button text dark (#111111)

// Destructive Actions — Strictly Slate/Charcoal (NO RED FONT)
val SlateDestructive = Color(0xFF374151)
val SlateDestructiveDark = Color(0xFF4B5563)
val WarmTerracotta = SlateDestructive
val WarmTerracottaDark = SlateDestructiveDark
