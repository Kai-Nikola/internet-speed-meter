package com.example.ui.theme

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
import com.example.data.model.ColorPalettePreset

@Composable
fun SpeedMeterTheme(
    darkTheme: Boolean = true, // Default to sleek tech dark theme
    palette: ColorPalettePreset = ColorPalettePreset.CYAN_NEON,
    content: @Composable () -> Unit
) {
    val primaryColor = Color(palette.primaryColorHex)
    val secondaryColor = Color(palette.uploadColorHex)
    val tertiaryColor = Color(palette.downloadColorHex)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = secondaryColor.copy(alpha = 0.2f),
            tertiary = tertiaryColor,
            background = BackgroundDark,
            surface = SurfaceDark,
            surfaceVariant = SurfaceCardDark,
            onBackground = TextPrimaryDark,
            onSurface = TextPrimaryDark,
            onSurfaceVariant = TextSecondaryDark,
            outline = SurfaceBorderDark
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = secondaryColor.copy(alpha = 0.15f),
            tertiary = tertiaryColor,
            background = Color(0xFFF8FAFC),
            surface = Color.White,
            surfaceVariant = SurfaceCardLight,
            onBackground = TextPrimaryLight,
            onSurface = TextPrimaryLight,
            onSurfaceVariant = TextSecondaryLight,
            outline = SurfaceBorderLight
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
