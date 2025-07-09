package com.hady.stonesforever.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val StonesForeverLightColors = lightColorScheme(
    primary = Color(0xFF23395B),         // Deep Sapphire
    onPrimary = Color(0xFFFFFFFF),       // White

    primaryContainer = Color(0xFF3C6E71), // Royal Blue
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFFE0C097),       // Champagne Gold
    onSecondary = Color(0xFF2D2D2D),     // Dark Gray

    tertiary = Color(0xFFB9B7BD),        // Warm Gray
    onTertiary = Color(0xFF1C1C1C),      // Charcoal

    background = Color(0xFFFAF9F6),      // Ivory
    onBackground = Color(0xFF1C1C1C),    // Charcoal

    surface = Color(0xFFFFFFFF),         // Pure White
    onSurface = Color(0xFF4F4F4F),       // Slate

    error = Color(0xFFA72608),           // Red Garnet
    onError = Color(0xFFFFFFFF)
)


val StonesForeverDarkColors = darkColorScheme(
    primary = Color(0xFF91B4D9),         // Muted Sapphire Blue
    onPrimary = Color(0xFF0D1B2A),       // Almost Black

    primaryContainer = Color(0xFF52799E),
    onPrimaryContainer = Color(0xFFE0E6ED),

    secondary = Color(0xFFD6B483),       // Muted Gold
    onSecondary = Color(0xFF121212),

    tertiary = Color(0xFF918F9A),
    onTertiary = Color(0xFFEDEDED),

    background = Color(0xFF121212),      // True Black
    onBackground = Color(0xFFF5F5F5),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),

    error = Color(0xFFFFB4A9),
    onError = Color(0xFF5C1A0A)
)


@Composable
fun StonesForeverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> StonesForeverDarkColors
        else -> StonesForeverLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}