package com.king_grey.notes_app.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.king_grey.notes_app.ui.theme.flowNotesFontFamily


@Composable
fun screenType(
    size: Float,
    weight: FontWeight,
    lineHeight: Float = size * 1.25f,
    letterSpacing: Float = 0f,
    tabular: Boolean = false,
): TextStyle {
    val fatMode = LocalFatModeEnabled.current
    // When Fat Mode is disabled, automatically apply a sleek, compact size factor (e.g., 90%)
    val effectiveSize = if (fatMode) size else size * 0.9f
    val effectiveLineHeight = if (fatMode) lineHeight else lineHeight * 0.9f

    return TextStyle(
        fontFamily = flowNotesFontFamily(effectiveSize),
        fontSize = effectiveSize.sp,
        fontWeight = weight,
        lineHeight = effectiveLineHeight.sp,
        letterSpacing = letterSpacing.sp,
        fontFeatureSettings = if (tabular) "tnum" else null,
    )
}
