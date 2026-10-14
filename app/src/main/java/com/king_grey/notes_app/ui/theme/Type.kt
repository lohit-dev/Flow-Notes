package com.king_grey.notes_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.king_grey.notes_app.R
import java.util.concurrent.ConcurrentHashMap

// The bundled variable font remains available offline and uses a size-matched optical axis.
private val extraBold = FontWeight(800)

private val familyCache = ConcurrentHashMap<Float, FontFamily>()

private fun robotoFlexFont(weight: FontWeight, opticalSize: Float) = Font(
    resId = R.font.roboto_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.opticalSizing(opticalSize.sp),
    ),
)

/** Roboto Flex with the optical-size axis set to [opticalSize]. */
fun flowNotesFontFamily(opticalSize: Float): FontFamily =
    familyCache.getOrPut(opticalSize) {
        FontFamily(
            robotoFlexFont(FontWeight.Normal, opticalSize),
            robotoFlexFont(FontWeight.Medium, opticalSize),
            robotoFlexFont(FontWeight.SemiBold, opticalSize),
            robotoFlexFont(FontWeight.Bold, opticalSize),
            robotoFlexFont(extraBold, opticalSize),
            robotoFlexFont(FontWeight.Black, opticalSize),
        )
    }

val baseline = Typography()

private fun TextStyle.flex(
    size: Float,
    lineHeight: Float,
    tracking: Float,
    weight: FontWeight,
) = copy(
    fontFamily = flowNotesFontFamily(size),
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
)

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.flex(57f, 64f, -0.25f, FontWeight.Normal),
    displayMedium = baseline.displayMedium.flex(45f, 52f, 0f, FontWeight.Normal),
    displaySmall = baseline.displaySmall.flex(36f, 44f, 0f, FontWeight.Normal),
    headlineLarge = baseline.headlineLarge.flex(32f, 40f, 0f, FontWeight.Normal),
    headlineMedium = baseline.headlineMedium.flex(28f, 36f, 0f, FontWeight.Normal),
    headlineSmall = baseline.headlineSmall.flex(24f, 32f, 0f, FontWeight.Normal),
    titleLarge = baseline.titleLarge.flex(22f, 28f, 0f, FontWeight.Normal),
    titleMedium = baseline.titleMedium.flex(16f, 24f, 0.15f, FontWeight.Medium),
    titleSmall = baseline.titleSmall.flex(14f, 20f, 0.1f, FontWeight.Medium),
    bodyLarge = baseline.bodyLarge.flex(16f, 24f, 0.5f, FontWeight.Normal),
    bodyMedium = baseline.bodyMedium.flex(14f, 20f, 0.25f, FontWeight.Normal),
    bodySmall = baseline.bodySmall.flex(12f, 16f, 0.4f, FontWeight.Bold),
    labelLarge = baseline.labelLarge.flex(14f, 20f, 0.1f, FontWeight.Medium),
    labelMedium = baseline.labelMedium.flex(12f, 16f, 0.5f, FontWeight.Medium),
    labelSmall = baseline.labelSmall.flex(11f, 16f, 0.5f, FontWeight.Medium),

    // Material 3 emphasized roles: same sizes, stronger bundled Roboto Flex weights.
    displayLargeEmphasized = baseline.displayLargeEmphasized.flex(
        57f,
        64f,
        -0.25f,
        FontWeight.Black
    ),
    displayMediumEmphasized = baseline.displayMediumEmphasized.flex(45f, 52f, 0f, FontWeight.Black),
    displaySmallEmphasized = baseline.displaySmallEmphasized.flex(36f, 44f, 0f, FontWeight.Black),
    headlineLargeEmphasized = baseline.headlineLargeEmphasized.flex(32f, 40f, 0f, FontWeight.Black),
    headlineMediumEmphasized = baseline.headlineMediumEmphasized.flex(
        28f, 36f, 0f, FontWeight.Black
    ),
    headlineSmallEmphasized = baseline.headlineSmallEmphasized.flex(24f, 32f, 0f, FontWeight.Black),
    titleLargeEmphasized = baseline.titleLargeEmphasized.flex(22f, 28f, 0f, FontWeight.SemiBold),
    titleMediumEmphasized = baseline.titleMediumEmphasized.flex(
        16f,
        24f,
        0.15f,
        FontWeight.SemiBold
    ),
    titleSmallEmphasized = baseline.titleSmallEmphasized.flex(14f, 20f, 0.1f, FontWeight.SemiBold),
    bodyLargeEmphasized = baseline.bodyLargeEmphasized.flex(16f, 24f, 0.5f, FontWeight.SemiBold),
    bodyMediumEmphasized = baseline.bodyMediumEmphasized.flex(14f, 20f, 0.25f, FontWeight.SemiBold),
    bodySmallEmphasized = baseline.bodySmallEmphasized.flex(12f, 16f, 0.4f, FontWeight.SemiBold),
    labelLargeEmphasized = baseline.labelLargeEmphasized.flex(14f, 20f, 0.1f, FontWeight.SemiBold),
    labelMediumEmphasized = baseline.labelMediumEmphasized.flex(
        12f,
        16f,
        0.5f,
        FontWeight.SemiBold
    ),
    labelSmallEmphasized = baseline.labelSmallEmphasized.flex(11f, 16f, 0.5f, FontWeight.SemiBold),
)
