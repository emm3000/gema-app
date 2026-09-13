package com.emm.gema.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.gema.core.ui.R

private val baseline: Typography = Typography()

val gemaFontFamily: FontFamily = FontFamily(
    Font(
        resId = R.font.ibm_plex_sans,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        resId = R.font.ibm_plex_sans,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        resId = R.font.ibm_plex_sans,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
)

val gemaTypography: Typography = Typography(
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = gemaFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 1.2.em,
    ),
    titleLarge = baseline.titleLarge.copy(
        fontFamily = gemaFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 1.3.em,
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = gemaFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 1.4.em,
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = gemaFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    ),
    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = gemaFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 1.45.em,
    ),
    bodyMedium = baseline.bodyMedium.copy(
        fontFamily = gemaFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 1.45.em,
    ),
    bodySmall = baseline.bodySmall.copy(
        fontFamily = gemaFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 1.45.em,
    ),
    labelLarge = baseline.labelLarge.copy(
        fontFamily = gemaFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = gemaFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    ),
    labelSmall = baseline.labelSmall.copy(
        fontFamily = gemaFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    ),
)
