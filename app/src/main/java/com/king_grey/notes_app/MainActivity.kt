package com.king_grey.notes_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.king_grey.notes_app.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // swaps Theme.FlowNotes.Starting for the normal theme
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box(Modifier.fillMaxSize()) {
                AppNavigation()
                SplashOverlay()
            }
        }
    }
}

/**
 * The animated half of the splash: the system shows plain indigo first, then this plays res/raw/splash.lottie
 * once over the app (which loads behind it) and fades away. Skipped after rotation or process-kept
 * restores, and dropped at once if the animation cannot be loaded.
 */
@Composable
private fun SplashOverlay() {
    var visible by rememberSaveable { mutableStateOf(true) }
    val result = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash))
    val progress by animateLottieCompositionAsState(result.value)

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
                .background(colorResource(R.color.splash_bg))
                // Swallow touches so nothing underneath reacts while the splash is up.
                .clickable(interactionSource = null, indication = null) {},
        ) {
            LottieAnimation(
                composition = result.value,
                progress = { progress },
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.Center),
            )
        }
    }
}
