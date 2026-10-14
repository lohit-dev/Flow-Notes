package com.king_grey.notes_app.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density

val LocalFatModeEnabled = staticCompositionLocalOf { false }

/**
 * Dynamically scales mockup geometry based on screen width and Fat Mode settings.
 */
@Composable
fun Scaled(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val widthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }

    // Calculate scale factor based on Fat Mode
    val scale = if (LocalFatModeEnabled.current) {
        // Fat Mode ON: Scale up to fill wider screens (capped so it doesn't get absurdly huge)
        (widthDp.value / 360f).coerceIn(1.0f, 1.35f)
    } else {
        // Fat Mode OFF (Compact / Slim mode):
        // Adapt gracefully to modern wider phones so typography and cards don't feel chunky.
        // E.g., targeting a ~390-400dp baseline with a slight allowable scale-down.
        (widthDp.value / 400f).coerceIn(0.88f, 1.0f)
    }

    CompositionLocalProvider(
        LocalDensity provides Density(
            density.density * scale,
            fontScale = density.fontScale,
        ),
        content = content,
    )
}
