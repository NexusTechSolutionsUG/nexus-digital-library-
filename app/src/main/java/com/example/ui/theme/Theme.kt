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
    primary = ScholasticNavyLight,
    onPrimary = DarkSlate,
    secondary = AcademicGoldLight,
    onSecondary = DarkSlate,
    tertiary = SageGreen,
    background = DarkSlate,
    surface = Color(0xFF121B2A), // Elegant customized card slate
    onBackground = ParchmentWhite,
    onSurface = ParchmentWhite,
    surfaceVariant = Color(0xFF1E293B), // High-tech card detail
    onSurfaceVariant = Color(0xFF94A3B8), // Muted details
    outlineVariant = Color(0xFF334155), // Premium subtle lines
    outline = Color(0xFF475569)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ScholasticNavy,
    onPrimary = ParchmentWhite,
    secondary = AcademicGold,
    onSecondary = ParchmentWhite,
    tertiary = SageGreen,
    background = ParchmentWhite,
    surface = SoftGray,
    onBackground = TextDark,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = TextLight,
    outlineVariant = BorderGray,
    outline = TextLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
