package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DepthPrimary,
    secondary = DepthSecondary,
    tertiary = DepthAccent,
    background = DepthBg,
    surface = DepthSurface,
    onPrimary = DepthBg,
    onSecondary = DepthBg,
    onTertiary = DepthBg,
    onBackground = DepthTextPrimary,
    onSurface = DepthTextPrimary
)

private val LightColorScheme = DarkColorScheme // Keep it dark and calm for the quiet room feel

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the meditative, quiet room vibe
    dynamicColor: Boolean = false, // Disable dynamic colors to keep our premium tailored dark palette
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
