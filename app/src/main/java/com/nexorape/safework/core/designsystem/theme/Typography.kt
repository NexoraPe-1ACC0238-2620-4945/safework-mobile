package com.nexorape.safework.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nexorape.safework.R

// Bundled OFL variable fonts; API 26 is the project minimum. No font network requests.
@OptIn(ExperimentalTextApi::class)
private fun family(resource: Int) = FontFamily(
    *listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map {
        Font(resource, weight = it, variationSettings = FontVariation.Settings(FontVariation.weight(it.weight)))
    }.toTypedArray()
)

private val Headings = family(R.font.raleway_variable)
private val Body = family(R.font.montserrat_variable)
private fun heading(size: Int, height: Int) = TextStyle(
    fontFamily = Headings, fontWeight = FontWeight.Bold, fontSize = size.sp,
    lineHeight = height.sp, letterSpacing = 0.sp,
)
private fun body(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Body, fontWeight = weight, fontSize = size.sp,
    lineHeight = height.sp, letterSpacing = 0.sp,
)

val SafeWorkTypography = Typography(
    displayLarge = heading(48, 56), displayMedium = heading(40, 48), displaySmall = heading(36, 44),
    headlineLarge = heading(32, 40), headlineMedium = heading(28, 36), headlineSmall = heading(24, 32),
    titleLarge = heading(22, 30), titleMedium = heading(18, 26), titleSmall = heading(16, 24),
    bodyLarge = body(16, 25), bodyMedium = body(14, 22), bodySmall = body(12, 19),
    labelLarge = body(14, 21, FontWeight.SemiBold), labelMedium = body(12, 18, FontWeight.SemiBold),
    labelSmall = body(11, 17, FontWeight.SemiBold),
)
