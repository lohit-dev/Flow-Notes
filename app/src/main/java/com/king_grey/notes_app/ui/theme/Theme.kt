package com.king_grey.notes_app.ui.theme

import android.app.WallpaperManager
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

private val violetLightScheme = lightColorScheme(
    primary = primaryVioletLight,
    onPrimary = onPrimaryVioletLight,
    primaryContainer = primaryContainerVioletLight,
    onPrimaryContainer = onPrimaryContainerVioletLight,
    secondary = secondaryVioletLight,
    onSecondary = onSecondaryVioletLight,
    secondaryContainer = secondaryContainerVioletLight,
    onSecondaryContainer = onSecondaryContainerVioletLight,
    tertiary = tertiaryVioletLight,
    onTertiary = onTertiaryVioletLight,
    tertiaryContainer = tertiaryContainerVioletLight,
    onTertiaryContainer = onTertiaryContainerVioletLight,
    error = errorVioletLight,
    onError = onErrorVioletLight,
    errorContainer = errorContainerVioletLight,
    onErrorContainer = onErrorContainerVioletLight,
    background = backgroundVioletLight,
    onBackground = onBackgroundVioletLight,
    surface = surfaceVioletLight,
    onSurface = onSurfaceVioletLight,
    surfaceVariant = surfaceVariantVioletLight,
    onSurfaceVariant = onSurfaceVariantVioletLight,
    outline = outlineVioletLight,
    outlineVariant = outlineVariantVioletLight,
    scrim = scrimVioletLight,
    inverseSurface = inverseSurfaceVioletLight,
    inverseOnSurface = inverseOnSurfaceVioletLight,
    inversePrimary = inversePrimaryVioletLight,
    surfaceDim = surfaceDimVioletLight,
    surfaceBright = surfaceBrightVioletLight,
    surfaceContainerLowest = surfaceContainerLowestVioletLight,
    surfaceContainerLow = surfaceContainerLowVioletLight,
    surfaceContainer = surfaceContainerVioletLight,
    surfaceContainerHigh = surfaceContainerHighVioletLight,
    surfaceContainerHighest = surfaceContainerHighestVioletLight,
)

private val violetDarkScheme = darkColorScheme(
    primary = primaryVioletDark,
    onPrimary = onPrimaryVioletDark,
    primaryContainer = primaryContainerVioletDark,
    onPrimaryContainer = onPrimaryContainerVioletDark,
    secondary = secondaryVioletDark,
    onSecondary = onSecondaryVioletDark,
    secondaryContainer = secondaryContainerVioletDark,
    onSecondaryContainer = onSecondaryContainerVioletDark,
    tertiary = tertiaryVioletDark,
    onTertiary = onTertiaryVioletDark,
    tertiaryContainer = tertiaryContainerVioletDark,
    onTertiaryContainer = onTertiaryContainerVioletDark,
    error = errorVioletDark,
    onError = onErrorVioletDark,
    errorContainer = errorContainerVioletDark,
    onErrorContainer = onErrorContainerVioletDark,
    background = backgroundVioletDark,
    onBackground = onBackgroundVioletDark,
    surface = surfaceVioletDark,
    onSurface = onSurfaceVioletDark,
    surfaceVariant = surfaceVariantVioletDark,
    onSurfaceVariant = onSurfaceVariantVioletDark,
    outline = outlineVioletDark,
    outlineVariant = outlineVariantVioletDark,
    scrim = scrimVioletDark,
    inverseSurface = inverseSurfaceVioletDark,
    inverseOnSurface = inverseOnSurfaceVioletDark,
    inversePrimary = inversePrimaryVioletDark,
    surfaceDim = surfaceDimVioletDark,
    surfaceBright = surfaceBrightVioletDark,
    surfaceContainerLowest = surfaceContainerLowestVioletDark,
    surfaceContainerLow = surfaceContainerLowVioletDark,
    surfaceContainer = surfaceContainerVioletDark,
    surfaceContainerHigh = surfaceContainerHighVioletDark,
    surfaceContainerHighest = surfaceContainerHighestVioletDark,
)

private val sapphireLightScheme = lightColorScheme(
    primary = primarySapphireLight,
    onPrimary = onPrimarySapphireLight,
    primaryContainer = primaryContainerSapphireLight,
    onPrimaryContainer = onPrimaryContainerSapphireLight,
    secondary = secondarySapphireLight,
    onSecondary = onSecondarySapphireLight,
    secondaryContainer = secondaryContainerSapphireLight,
    onSecondaryContainer = onSecondaryContainerSapphireLight,
    tertiary = tertiarySapphireLight,
    onTertiary = onTertiarySapphireLight,
    tertiaryContainer = tertiaryContainerSapphireLight,
    onTertiaryContainer = onTertiaryContainerSapphireLight,
    error = errorSapphireLight,
    onError = onErrorSapphireLight,
    errorContainer = errorContainerSapphireLight,
    onErrorContainer = onErrorContainerSapphireLight,
    background = backgroundSapphireLight,
    onBackground = onBackgroundSapphireLight,
    surface = surfaceSapphireLight,
    onSurface = onSurfaceSapphireLight,
    surfaceVariant = surfaceVariantSapphireLight,
    onSurfaceVariant = onSurfaceVariantSapphireLight,
    outline = outlineSapphireLight,
    outlineVariant = outlineVariantSapphireLight,
    scrim = scrimSapphireLight,
    inverseSurface = inverseSurfaceSapphireLight,
    inverseOnSurface = inverseOnSurfaceSapphireLight,
    inversePrimary = inversePrimarySapphireLight,
    surfaceDim = surfaceDimSapphireLight,
    surfaceBright = surfaceBrightSapphireLight,
    surfaceContainerLowest = surfaceContainerLowestSapphireLight,
    surfaceContainerLow = surfaceContainerLowSapphireLight,
    surfaceContainer = surfaceContainerSapphireLight,
    surfaceContainerHigh = surfaceContainerHighSapphireLight,
    surfaceContainerHighest = surfaceContainerHighestSapphireLight,
)

private val sapphireDarkScheme = darkColorScheme(
    primary = primarySapphireDark,
    onPrimary = onPrimarySapphireDark,
    primaryContainer = primaryContainerSapphireDark,
    onPrimaryContainer = onPrimaryContainerSapphireDark,
    secondary = secondarySapphireDark,
    onSecondary = onSecondarySapphireDark,
    secondaryContainer = secondaryContainerSapphireDark,
    onSecondaryContainer = onSecondaryContainerSapphireDark,
    tertiary = tertiarySapphireDark,
    onTertiary = onTertiarySapphireDark,
    tertiaryContainer = tertiaryContainerSapphireDark,
    onTertiaryContainer = onTertiaryContainerSapphireDark,
    error = errorSapphireDark,
    onError = onErrorSapphireDark,
    errorContainer = errorContainerSapphireDark,
    onErrorContainer = onErrorContainerSapphireDark,
    background = backgroundSapphireDark,
    onBackground = onBackgroundSapphireDark,
    surface = surfaceSapphireDark,
    onSurface = onSurfaceSapphireDark,
    surfaceVariant = surfaceVariantSapphireDark,
    onSurfaceVariant = onSurfaceVariantSapphireDark,
    outline = outlineSapphireDark,
    outlineVariant = outlineVariantSapphireDark,
    scrim = scrimSapphireDark,
    inverseSurface = inverseSurfaceSapphireDark,
    inverseOnSurface = inverseOnSurfaceSapphireDark,
    inversePrimary = inversePrimarySapphireDark,
    surfaceDim = surfaceDimSapphireDark,
    surfaceBright = surfaceBrightSapphireDark,
    surfaceContainerLowest = surfaceContainerLowestSapphireDark,
    surfaceContainerLow = surfaceContainerLowSapphireDark,
    surfaceContainer = surfaceContainerSapphireDark,
    surfaceContainerHigh = surfaceContainerHighSapphireDark,
    surfaceContainerHighest = surfaceContainerHighestSapphireDark,
)

private val meadowLightScheme = lightColorScheme(
    primary = primaryMeadowLight,
    onPrimary = onPrimaryMeadowLight,
    primaryContainer = primaryContainerMeadowLight,
    onPrimaryContainer = onPrimaryContainerMeadowLight,
    secondary = secondaryMeadowLight,
    onSecondary = onSecondaryMeadowLight,
    secondaryContainer = secondaryContainerMeadowLight,
    onSecondaryContainer = onSecondaryContainerMeadowLight,
    tertiary = tertiaryMeadowLight,
    onTertiary = onTertiaryMeadowLight,
    tertiaryContainer = tertiaryContainerMeadowLight,
    onTertiaryContainer = onTertiaryContainerMeadowLight,
    error = errorMeadowLight,
    onError = onErrorMeadowLight,
    errorContainer = errorContainerMeadowLight,
    onErrorContainer = onErrorContainerMeadowLight,
    background = backgroundMeadowLight,
    onBackground = onBackgroundMeadowLight,
    surface = surfaceMeadowLight,
    onSurface = onSurfaceMeadowLight,
    surfaceVariant = surfaceVariantMeadowLight,
    onSurfaceVariant = onSurfaceVariantMeadowLight,
    outline = outlineMeadowLight,
    outlineVariant = outlineVariantMeadowLight,
    scrim = scrimMeadowLight,
    inverseSurface = inverseSurfaceMeadowLight,
    inverseOnSurface = inverseOnSurfaceMeadowLight,
    inversePrimary = inversePrimaryMeadowLight,
    surfaceDim = surfaceDimMeadowLight,
    surfaceBright = surfaceBrightMeadowLight,
    surfaceContainerLowest = surfaceContainerLowestMeadowLight,
    surfaceContainerLow = surfaceContainerLowMeadowLight,
    surfaceContainer = surfaceContainerMeadowLight,
    surfaceContainerHigh = surfaceContainerHighMeadowLight,
    surfaceContainerHighest = surfaceContainerHighestMeadowLight,
)

private val meadowDarkScheme = darkColorScheme(
    primary = primaryMeadowDark,
    onPrimary = onPrimaryMeadowDark,
    primaryContainer = primaryContainerMeadowDark,
    onPrimaryContainer = onPrimaryContainerMeadowDark,
    secondary = secondaryMeadowDark,
    onSecondary = onSecondaryMeadowDark,
    secondaryContainer = secondaryContainerMeadowDark,
    onSecondaryContainer = onSecondaryContainerMeadowDark,
    tertiary = tertiaryMeadowDark,
    onTertiary = onTertiaryMeadowDark,
    tertiaryContainer = tertiaryContainerMeadowDark,
    onTertiaryContainer = onTertiaryContainerMeadowDark,
    error = errorMeadowDark,
    onError = onErrorMeadowDark,
    errorContainer = errorContainerMeadowDark,
    onErrorContainer = onErrorContainerMeadowDark,
    background = backgroundMeadowDark,
    onBackground = onBackgroundMeadowDark,
    surface = surfaceMeadowDark,
    onSurface = onSurfaceMeadowDark,
    surfaceVariant = surfaceVariantMeadowDark,
    onSurfaceVariant = onSurfaceVariantMeadowDark,
    outline = outlineMeadowDark,
    outlineVariant = outlineVariantMeadowDark,
    scrim = scrimMeadowDark,
    inverseSurface = inverseSurfaceMeadowDark,
    inverseOnSurface = inverseOnSurfaceMeadowDark,
    inversePrimary = inversePrimaryMeadowDark,
    surfaceDim = surfaceDimMeadowDark,
    surfaceBright = surfaceBrightMeadowDark,
    surfaceContainerLowest = surfaceContainerLowestMeadowDark,
    surfaceContainerLow = surfaceContainerLowMeadowDark,
    surfaceContainer = surfaceContainerMeadowDark,
    surfaceContainerHigh = surfaceContainerHighMeadowDark,
    surfaceContainerHighest = surfaceContainerHighestMeadowDark,
)

private val sunsetLightScheme = lightColorScheme(
    primary = primarySunsetLight,
    onPrimary = onPrimarySunsetLight,
    primaryContainer = primaryContainerSunsetLight,
    onPrimaryContainer = onPrimaryContainerSunsetLight,
    secondary = secondarySunsetLight,
    onSecondary = onSecondarySunsetLight,
    secondaryContainer = secondaryContainerSunsetLight,
    onSecondaryContainer = onSecondaryContainerSunsetLight,
    tertiary = tertiarySunsetLight,
    onTertiary = onTertiarySunsetLight,
    tertiaryContainer = tertiaryContainerSunsetLight,
    onTertiaryContainer = onTertiaryContainerSunsetLight,
    error = errorSunsetLight,
    onError = onErrorSunsetLight,
    errorContainer = errorContainerSunsetLight,
    onErrorContainer = onErrorContainerSunsetLight,
    background = backgroundSunsetLight,
    onBackground = onBackgroundSunsetLight,
    surface = surfaceSunsetLight,
    onSurface = onSurfaceSunsetLight,
    surfaceVariant = surfaceVariantSunsetLight,
    onSurfaceVariant = onSurfaceVariantSunsetLight,
    outline = outlineSunsetLight,
    outlineVariant = outlineVariantSunsetLight,
    scrim = scrimSunsetLight,
    inverseSurface = inverseSurfaceSunsetLight,
    inverseOnSurface = inverseOnSurfaceSunsetLight,
    inversePrimary = inversePrimarySunsetLight,
    surfaceDim = surfaceDimSunsetLight,
    surfaceBright = surfaceBrightSunsetLight,
    surfaceContainerLowest = surfaceContainerLowestSunsetLight,
    surfaceContainerLow = surfaceContainerLowSunsetLight,
    surfaceContainer = surfaceContainerSunsetLight,
    surfaceContainerHigh = surfaceContainerHighSunsetLight,
    surfaceContainerHighest = surfaceContainerHighestSunsetLight,
)

private val sunsetDarkScheme = darkColorScheme(
    primary = primarySunsetDark,
    onPrimary = onPrimarySunsetDark,
    primaryContainer = primaryContainerSunsetDark,
    onPrimaryContainer = onPrimaryContainerSunsetDark,
    secondary = secondarySunsetDark,
    onSecondary = onSecondarySunsetDark,
    secondaryContainer = secondaryContainerSunsetDark,
    onSecondaryContainer = onSecondaryContainerSunsetDark,
    tertiary = tertiarySunsetDark,
    onTertiary = onTertiarySunsetDark,
    tertiaryContainer = tertiaryContainerSunsetDark,
    onTertiaryContainer = onTertiaryContainerSunsetDark,
    error = errorSunsetDark,
    onError = onErrorSunsetDark,
    errorContainer = errorContainerSunsetDark,
    onErrorContainer = onErrorContainerSunsetDark,
    background = backgroundSunsetDark,
    onBackground = onBackgroundSunsetDark,
    surface = surfaceSunsetDark,
    onSurface = onSurfaceSunsetDark,
    surfaceVariant = surfaceVariantSunsetDark,
    onSurfaceVariant = onSurfaceVariantSunsetDark,
    outline = outlineSunsetDark,
    outlineVariant = outlineVariantSunsetDark,
    scrim = scrimSunsetDark,
    inverseSurface = inverseSurfaceSunsetDark,
    inverseOnSurface = inverseOnSurfaceSunsetDark,
    inversePrimary = inversePrimarySunsetDark,
    surfaceDim = surfaceDimSunsetDark,
    surfaceBright = surfaceBrightSunsetDark,
    surfaceContainerLowest = surfaceContainerLowestSunsetDark,
    surfaceContainerLow = surfaceContainerLowSunsetDark,
    surfaceContainer = surfaceContainerSunsetDark,
    surfaceContainerHigh = surfaceContainerHighSunsetDark,
    surfaceContainerHighest = surfaceContainerHighestSunsetDark,
)

private val orchidLightScheme = lightColorScheme(
    primary = primaryOrchidLight,
    onPrimary = onPrimaryOrchidLight,
    primaryContainer = primaryContainerOrchidLight,
    onPrimaryContainer = onPrimaryContainerOrchidLight,
    secondary = secondaryOrchidLight,
    onSecondary = onSecondaryOrchidLight,
    secondaryContainer = secondaryContainerOrchidLight,
    onSecondaryContainer = onSecondaryContainerOrchidLight,
    tertiary = tertiaryOrchidLight,
    onTertiary = onTertiaryOrchidLight,
    tertiaryContainer = tertiaryContainerOrchidLight,
    onTertiaryContainer = onTertiaryContainerOrchidLight,
    error = errorOrchidLight,
    onError = onErrorOrchidLight,
    errorContainer = errorContainerOrchidLight,
    onErrorContainer = onErrorContainerOrchidLight,
    background = backgroundOrchidLight,
    onBackground = onBackgroundOrchidLight,
    surface = surfaceOrchidLight,
    onSurface = onSurfaceOrchidLight,
    surfaceVariant = surfaceVariantOrchidLight,
    onSurfaceVariant = onSurfaceVariantOrchidLight,
    outline = outlineOrchidLight,
    outlineVariant = outlineVariantOrchidLight,
    scrim = scrimOrchidLight,
    inverseSurface = inverseSurfaceOrchidLight,
    inverseOnSurface = inverseOnSurfaceOrchidLight,
    inversePrimary = inversePrimaryOrchidLight,
    surfaceDim = surfaceDimOrchidLight,
    surfaceBright = surfaceBrightOrchidLight,
    surfaceContainerLowest = surfaceContainerLowestOrchidLight,
    surfaceContainerLow = surfaceContainerLowOrchidLight,
    surfaceContainer = surfaceContainerOrchidLight,
    surfaceContainerHigh = surfaceContainerHighOrchidLight,
    surfaceContainerHighest = surfaceContainerHighestOrchidLight,
)

private val orchidDarkScheme = darkColorScheme(
    primary = primaryOrchidDark,
    onPrimary = onPrimaryOrchidDark,
    primaryContainer = primaryContainerOrchidDark,
    onPrimaryContainer = onPrimaryContainerOrchidDark,
    secondary = secondaryOrchidDark,
    onSecondary = onSecondaryOrchidDark,
    secondaryContainer = secondaryContainerOrchidDark,
    onSecondaryContainer = onSecondaryContainerOrchidDark,
    tertiary = tertiaryOrchidDark,
    onTertiary = onTertiaryOrchidDark,
    tertiaryContainer = tertiaryContainerOrchidDark,
    onTertiaryContainer = onTertiaryContainerOrchidDark,
    error = errorOrchidDark,
    onError = onErrorOrchidDark,
    errorContainer = errorContainerOrchidDark,
    onErrorContainer = onErrorContainerOrchidDark,
    background = backgroundOrchidDark,
    onBackground = onBackgroundOrchidDark,
    surface = surfaceOrchidDark,
    onSurface = onSurfaceOrchidDark,
    surfaceVariant = surfaceVariantOrchidDark,
    onSurfaceVariant = onSurfaceVariantOrchidDark,
    outline = outlineOrchidDark,
    outlineVariant = outlineVariantOrchidDark,
    scrim = scrimOrchidDark,
    inverseSurface = inverseSurfaceOrchidDark,
    inverseOnSurface = inverseOnSurfaceOrchidDark,
    inversePrimary = inversePrimaryOrchidDark,
    surfaceDim = surfaceDimOrchidDark,
    surfaceBright = surfaceBrightOrchidDark,
    surfaceContainerLowest = surfaceContainerLowestOrchidDark,
    surfaceContainerLow = surfaceContainerLowOrchidDark,
    surfaceContainer = surfaceContainerOrchidDark,
    surfaceContainerHigh = surfaceContainerHighOrchidDark,
    surfaceContainerHighest = surfaceContainerHighestOrchidDark,
)

private val monochromeLightScheme = lightColorScheme(
    primary = primaryMonochromeLight,
    onPrimary = onPrimaryMonochromeLight,
    primaryContainer = primaryContainerMonochromeLight,
    onPrimaryContainer = onPrimaryContainerMonochromeLight,
    secondary = secondaryMonochromeLight,
    onSecondary = onSecondaryMonochromeLight,
    secondaryContainer = secondaryContainerMonochromeLight,
    onSecondaryContainer = onSecondaryContainerMonochromeLight,
    tertiary = tertiaryMonochromeLight,
    onTertiary = onTertiaryMonochromeLight,
    tertiaryContainer = tertiaryContainerMonochromeLight,
    onTertiaryContainer = onTertiaryContainerMonochromeLight,
    error = errorMonochromeLight,
    onError = onErrorMonochromeLight,
    errorContainer = errorContainerMonochromeLight,
    onErrorContainer = onErrorContainerMonochromeLight,
    background = backgroundMonochromeLight,
    onBackground = onBackgroundMonochromeLight,
    surface = surfaceMonochromeLight,
    onSurface = onSurfaceMonochromeLight,
    surfaceVariant = surfaceVariantMonochromeLight,
    onSurfaceVariant = onSurfaceVariantMonochromeLight,
    outline = outlineMonochromeLight,
    outlineVariant = outlineVariantMonochromeLight,
    scrim = scrimMonochromeLight,
    inverseSurface = inverseSurfaceMonochromeLight,
    inverseOnSurface = inverseOnSurfaceMonochromeLight,
    inversePrimary = inversePrimaryMonochromeLight,
    surfaceDim = surfaceDimMonochromeLight,
    surfaceBright = surfaceBrightMonochromeLight,
    surfaceContainerLowest = surfaceContainerLowestMonochromeLight,
    surfaceContainerLow = surfaceContainerLowMonochromeLight,
    surfaceContainer = surfaceContainerMonochromeLight,
    surfaceContainerHigh = surfaceContainerHighMonochromeLight,
    surfaceContainerHighest = surfaceContainerHighestMonochromeLight,
)

private val monochromeDarkScheme = darkColorScheme(
    primary = primaryMonochromeDark,
    onPrimary = onPrimaryMonochromeDark,
    primaryContainer = primaryContainerMonochromeDark,
    onPrimaryContainer = onPrimaryContainerMonochromeDark,
    secondary = secondaryMonochromeDark,
    onSecondary = onSecondaryMonochromeDark,
    secondaryContainer = secondaryContainerMonochromeDark,
    onSecondaryContainer = onSecondaryContainerMonochromeDark,
    tertiary = tertiaryMonochromeDark,
    onTertiary = onTertiaryMonochromeDark,
    tertiaryContainer = tertiaryContainerMonochromeDark,
    onTertiaryContainer = onTertiaryContainerMonochromeDark,
    error = errorMonochromeDark,
    onError = onErrorMonochromeDark,
    errorContainer = errorContainerMonochromeDark,
    onErrorContainer = onErrorContainerMonochromeDark,
    background = backgroundMonochromeDark,
    onBackground = onBackgroundMonochromeDark,
    surface = surfaceMonochromeDark,
    onSurface = onSurfaceMonochromeDark,
    surfaceVariant = surfaceVariantMonochromeDark,
    onSurfaceVariant = onSurfaceVariantMonochromeDark,
    outline = outlineMonochromeDark,
    outlineVariant = outlineVariantMonochromeDark,
    scrim = scrimMonochromeDark,
    inverseSurface = inverseSurfaceMonochromeDark,
    inverseOnSurface = inverseOnSurfaceMonochromeDark,
    inversePrimary = inversePrimaryMonochromeDark,
    surfaceDim = surfaceDimMonochromeDark,
    surfaceBright = surfaceBrightMonochromeDark,
    surfaceContainerLowest = surfaceContainerLowestMonochromeDark,
    surfaceContainerLow = surfaceContainerLowMonochromeDark,
    surfaceContainer = surfaceContainerMonochromeDark,
    surfaceContainerHigh = surfaceContainerHighMonochromeDark,
    surfaceContainerHighest = surfaceContainerHighestMonochromeDark,
)

enum class FlowNotesPalette {
    Sapphire,
    Meadow,
    Sunset,
    Orchid,
    Violet,
    Monochrome,
}

private fun FlowNotesPalette.colorScheme(isDark: Boolean) = when (this) {
    FlowNotesPalette.Violet -> if (isDark) violetDarkScheme else violetLightScheme
    FlowNotesPalette.Sapphire -> if (isDark) sapphireDarkScheme else sapphireLightScheme
    FlowNotesPalette.Meadow -> if (isDark) meadowDarkScheme else meadowLightScheme
    FlowNotesPalette.Sunset -> if (isDark) sunsetDarkScheme else sunsetLightScheme
    FlowNotesPalette.Orchid -> if (isDark) orchidDarkScheme else orchidLightScheme
    FlowNotesPalette.Monochrome -> if (isDark) monochromeDarkScheme else monochromeLightScheme
}

@Composable
private fun rememberWallpaperSeed(): Color? {
    val context = LocalContext.current
    val manager = remember(context) { WallpaperManager.getInstance(context) }
    var seed by remember(manager) {
        mutableStateOf(
            manager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?.primaryColor?.let { Color(it.toArgb()) }
        )
    }

    DisposableEffect(manager) {
        val listener = WallpaperManager.OnColorsChangedListener { colors, which ->
            if (which and WallpaperManager.FLAG_SYSTEM != 0) {
                seed = colors?.primaryColor?.let { Color(it.toArgb()) }
            }
        }
        manager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        onDispose { manager.removeOnColorsChangedListener(listener) }
    }

    return seed
}

@Composable
fun FlowNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    palette: FlowNotesPalette = FlowNotesPalette.Sapphire,
    content: @Composable () -> Unit
) {
    val seed = if (dynamicColor) {
        rememberWallpaperSeed()
    } else null

    val colorScheme = if (seed != null) {
        rememberDynamicColorScheme(
            seedColor = seed,
            isDark = darkTheme,
            style = paletteStyle
        )
    } else {
        palette.colorScheme(isDark = darkTheme)
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
//        shapes = FlowNotesMaterialShapes,
        typography = AppTypography,
        content = content
    )
}
