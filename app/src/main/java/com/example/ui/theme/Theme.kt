package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisColorScheme = darkColorScheme(
  primary = ArcCyan,
  onPrimary = HoloDarkBg,
  primaryContainer = HoloSurfaceElevated,
  onPrimaryContainer = ArcCyanGlow,
  secondary = ArcBlueCore,
  onSecondary = HoloDarkBg,
  secondaryContainer = HoloSurface,
  onSecondaryContainer = ArcCyanGlow,
  tertiary = StarkGold,
  onTertiary = HoloDarkBg,
  background = HoloDarkBg,
  onBackground = HoloTextPrimary,
  surface = HoloSurface,
  onSurface = HoloTextPrimary,
  surfaceVariant = HoloSurfaceElevated,
  onSurfaceVariant = HoloTextSecondary,
  outline = ArcCyan.copy(alpha = 0.3f),
  error = StatusError,
  onError = HoloDarkBg
)

private val JarvisLightColorScheme = lightColorScheme(
  primary = Color(0xFF0078FF),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE8EEF8),
  onPrimaryContainer = Color(0xFF0055BB),
  secondary = Color(0xFF0078FF),
  onSecondary = Color.White,
  secondaryContainer = Color.White,
  onSecondaryContainer = Color(0xFF1A2332),
  tertiary = StarkGold,
  onTertiary = Color.White,
  background = Color(0xFFF5F7FB),
  onBackground = Color(0xFF1A2332),
  surface = Color.White,
  onSurface = Color(0xFF1A2332),
  surfaceVariant = Color(0xFFEAEFF7),
  onSurfaceVariant = Color(0xFF5A6E85),
  outline = Color(0xFF0078FF).copy(alpha = 0.3f),
  error = StatusError,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  val statusBarColor = if (darkTheme) HoloDarkBg else Color(0xFFF5F7FB)
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = statusBarColor.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = if (darkTheme) JarvisColorScheme else JarvisLightColorScheme,
    typography = Typography,
    content = content
  )
}
