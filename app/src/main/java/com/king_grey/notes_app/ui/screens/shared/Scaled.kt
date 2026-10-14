package com.king_grey.notes_app.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Kept so existing call sites still compile. The screens are drawn 1:1 with the mockup (1 mockup
 * px = 1dp, 1 mockup font px = 1sp), so nothing is scaled any more. The old "compact" mode shrank
 * every dp by up to 12% and every font by 10%, which is why the app did not match the design.
 */
val LocalFatModeEnabled = staticCompositionLocalOf { false }

@Composable
fun Scaled(content: @Composable () -> Unit) {
    content()
}
