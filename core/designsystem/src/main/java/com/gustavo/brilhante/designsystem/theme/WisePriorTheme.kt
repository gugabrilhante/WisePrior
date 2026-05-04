package com.gustavo.brilhante.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = OnGreen40,
    primaryContainer = GreenContainer40,
    onPrimaryContainer = OnGreenContainer40,
    secondary = BlueGrey40,
    tertiary = Teal40
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = OnGreen80,
    primaryContainer = GreenContainer80,
    onPrimaryContainer = OnGreenContainer80,
    secondary = NeutralBlue80,
    onSecondary = OnNeutralBlue80,
    secondaryContainer = NeutralBlueContainer80,
    onSecondaryContainer = OnNeutralBlueContainer80,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

@Composable
fun WisePriorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color overrides the designed palette on Android 12+; disabled so
    // the custom dark theme and green accent always apply consistently.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WisePriorTypography,
        shapes = WisePriorShapes,
        content = content
    )
}
