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

private val DarkColorScheme =
  darkColorScheme(
    primary = IndigoPrimaryDark,
    secondary = IndigoSecondaryDark,
    tertiary = IndigoTertiaryDark,
    background = SlateBackgroundDark,
    surface = SlateSurfaceDark,
    onPrimary = SlateBackgroundDark,
    onSecondary = SlateBackgroundDark,
    onTertiary = SlateBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = IndigoPrimary,
    secondary = IndigoSecondary,
    tertiary = IndigoTertiary,
    background = SlateBackgroundLight,
    surface = SlateSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  themeAccent: String = "Indigo",
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Set to false to enforce our elegant slate & indigo brand colors!
  content: @Composable () -> Unit,
) {
  val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  // Override primary and secondary based on accent selected dynamically
  val accentColors = AccentColors[themeAccent] ?: AccentColors["Indigo"]!!
  val overridenScheme = if (darkTheme) {
    baseScheme.copy(
      primary = accentColors[2],
      secondary = accentColors[3]
    )
  } else {
    baseScheme.copy(
      primary = accentColors[0],
      secondary = accentColors[1]
    )
  }

  MaterialTheme(colorScheme = overridenScheme, typography = Typography, content = content)
}
