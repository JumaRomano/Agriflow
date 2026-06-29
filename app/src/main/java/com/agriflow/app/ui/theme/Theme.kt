 /**
 * Core helper component: Theme.
 */
package com.agriflow.app.ui.theme

import android.app.Activity
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
    primary = AgriflowPrimaryLight,
    onPrimary = AgriflowOnPrimaryLight,
    primaryContainer = AgriflowPrimaryContainerLight,
    secondary = AgriflowSecondaryLight,
    tertiary = AgriflowTertiaryLight,
    background = AgriflowBackgroundLight,
    surface = AgriflowSurfaceLight,
    onBackground = AgriflowOnBackgroundLight,
    onSurface = AgriflowOnSurfaceLight,
    error = AgriflowErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = AgriflowPrimaryDark,
    onPrimary = AgriflowOnPrimaryDark,
    primaryContainer = AgriflowPrimaryContainerDark,
    onPrimaryContainer = AgriflowOnPrimaryContainerDark,
    secondary = AgriflowSecondaryDark,
    onSecondary = AgriflowOnSecondaryDark,
    tertiary = AgriflowTertiaryDark,
    onTertiary = AgriflowOnTertiaryDark,
    background = AgriflowBackgroundDark,
    surface = AgriflowSurfaceDark,
    onBackground = AgriflowOnBackgroundDark,
    onSurface = AgriflowOnSurfaceDark,
    error = AgriflowErrorDark,
    onError = AgriflowOnErrorDark
)

@Composable
fun AgriflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, but disabled by default to maintain brand consistency
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
        typography = Typography,
        content = content
    )
}