package com.king_grey.notes_app.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.king_grey.notes_app.ui.theme.flowNotesFontFamily

/**
 * A text style taken straight from the mockup: [size] and [lineHeight] are the CSS px values and
 * become sp 1:1 (the Text size setting still applies through the font scale).
 */
@Composable
fun screenType(
    size: Float,
    weight: FontWeight,
    lineHeight: Float = size * 1.25f,
    letterSpacing: Float = 0f,
    tabular: Boolean = false,
): TextStyle = TextStyle(
    fontFamily = flowNotesFontFamily(size),
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    fontFeatureSettings = if (tabular) "tnum" else null,
)
