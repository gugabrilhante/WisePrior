package com.gustavo.brilhante.designsystem.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.gustavo.brilhante.designsystem.R

@Composable
fun WisePriorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color overrides the designed palette on Android 12+; disabled so
    // the custom dark theme and green accent always apply consistently.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val lightColorScheme = lightColorScheme(
        primary = colorResource(R.color.green_40),
        onPrimary = colorResource(R.color.on_green_40),
        primaryContainer = colorResource(R.color.green_container_40),
        onPrimaryContainer = colorResource(R.color.on_green_container_40),
        secondary = colorResource(R.color.blue_grey_40),
        tertiary = colorResource(R.color.teal_40)
    )

    val darkColorScheme = darkColorScheme(
        primary = colorResource(R.color.green_80),
        onPrimary = colorResource(R.color.on_green_80),
        primaryContainer = colorResource(R.color.green_container_80),
        onPrimaryContainer = colorResource(R.color.on_green_container_80),
        secondary = colorResource(R.color.neutral_blue_80),
        onSecondary = colorResource(R.color.on_neutral_blue_80),
        secondaryContainer = colorResource(R.color.neutral_blue_container_80),
        onSecondaryContainer = colorResource(R.color.on_neutral_blue_container_80),
        background = colorResource(R.color.surface_dark),
        surface = colorResource(R.color.surface_dark),
        surfaceVariant = colorResource(R.color.surface_variant_dark),
        surfaceContainerLowest = colorResource(R.color.surface_container_lowest_dark),
        surfaceContainerLow = colorResource(R.color.surface_container_low_dark),
        surfaceContainer = colorResource(R.color.surface_container_dark),
        surfaceContainerHigh = colorResource(R.color.surface_container_high_dark),
        surfaceContainerHighest = colorResource(R.color.surface_container_highest_dark),
        onBackground = colorResource(R.color.on_surface_dark),
        onSurface = colorResource(R.color.on_surface_dark),
        onSurfaceVariant = colorResource(R.color.on_surface_variant_dark),
        outline = colorResource(R.color.outline_dark),
        outlineVariant = colorResource(R.color.outline_variant_dark),
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WisePriorTypography,
        shapes = WisePriorShapes,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
