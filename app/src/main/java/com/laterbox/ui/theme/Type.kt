package com.laterbox.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.laterbox.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val PlayfairGoogleFont = GoogleFont("Playfair Display")
val InterGoogleFont = GoogleFont("Inter")
val PoppinsGoogleFont = GoogleFont("Poppins")

// High-contrast editorial Serif font matching reference screenshots
val SerifHeadlineFontFamily = FontFamily(
    Font(googleFont = PlayfairGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = PlayfairGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = PlayfairGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PlayfairGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Inter modern geometric sans font family
val InterFontFamily = FontFamily(
    Font(googleFont = InterGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = InterGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = InterGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = InterGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Poppins expressive modern sans font family
val PoppinsFontFamily = FontFamily(
    Font(googleFont = PoppinsGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = PoppinsGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = PoppinsGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = PoppinsGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Typography matching the editorial Serif + clean Sans (Inter/Poppins) aesthetic
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SerifHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SerifHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SerifHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SerifHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SerifHeadlineFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SerifHeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
