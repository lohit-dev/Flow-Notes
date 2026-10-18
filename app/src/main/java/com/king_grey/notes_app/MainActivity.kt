package com.king_grey.notes_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.king_grey.notes_app.navigation.AppNavigation
import com.king_grey.notes_app.ui.viewmodel.NotesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Same instance AppNavigation gets from hiltViewModel(): both live in the activity's store.
    private val viewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hold the system splash until the first read from disk is done, so the theme (accent,
        // dark mode, dynamic colour) is known before the animation draws.
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.settings.value == null || viewModel.notes.value == null || viewModel.folders.value == null
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation(viewModel = viewModel, overlay = { SplashOverlay() })
        }
    }
}

/**
 * The animated half of the splash. It sits inside the app theme, so res/raw/splash.json is
 * recoloured from the current colour scheme: every accent, light and dark, and dynamic colour.
 * Plays once over the app, then fades. Not replayed after rotation, and dropped at once if the
 * animation cannot be loaded.
 */
@Composable
private fun SplashOverlay() {
    var visible by rememberSaveable { mutableStateOf(true) }
    val result = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash))
    val progress by animateLottieCompositionAsState(result.value)

    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < 0.5f
    val properties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, colors.primary.toArgb(), "**", "card_fill"),
        rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, colors.onPrimary.toArgb(), "**", "wave1_stroke"),
        rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, colors.onPrimary.toArgb(), "**", "wave2_stroke"),
        rememberLottieDynamicProperty(
            LottieProperty.COLOR,
            (if (dark) colors.tertiary else colors.tertiaryContainer).toArgb(),
            "**",
            "dot_fill",
        ),
    )

    LaunchedEffect(result.isFailure) { if (result.isFailure) visible = false }
    LaunchedEffect(progress) {
        if (progress >= 1f) {
            delay(150.milliseconds)
            visible = false
        }
    }

    AnimatedVisibility(visible = visible, exit = fadeOut(tween(300))) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                // Swallow touches so nothing underneath reacts while the splash is up.
                .clickable(interactionSource = null, indication = null) {},
        ) {
            LottieAnimation(
                composition = result.value,
                progress = { progress },
                dynamicProperties = properties,
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.Center),
            )
        }
    }
}
