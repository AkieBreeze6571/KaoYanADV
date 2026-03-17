package com.example.kaoyanadventure.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaoyanadventure.data.AppThemeMode

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF4F46E5),      // indigo
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = androidx.compose.ui.graphics.Color(0xFF06B6D4),    // cyan
    onSecondary = androidx.compose.ui.graphics.Color(0xFF001018),
    tertiary = androidx.compose.ui.graphics.Color(0xFFF97316),     // orange
    onTertiary = androidx.compose.ui.graphics.Color(0xFF2B0B00),
    background = androidx.compose.ui.graphics.Color(0xFFF7F7FB),
    onBackground = androidx.compose.ui.graphics.Color(0xFF0F172A),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF0F172A),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF1F2F7),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF475569),
    outline = androidx.compose.ui.graphics.Color(0xFFD0D5DD)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF818CF8),      // indigo 300
    onPrimary = androidx.compose.ui.graphics.Color(0xFF0B1026),
    secondary = androidx.compose.ui.graphics.Color(0xFF22D3EE),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF001018),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFB923C),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF2B0B00),
    background = androidx.compose.ui.graphics.Color(0xFF0B0F1A),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    surface = androidx.compose.ui.graphics.Color(0xFF101626),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF151C2F),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFB6C0D1),
    outline = androidx.compose.ui.graphics.Color(0xFF2B3550)
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun KaoyanTheme(mode: AppThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) DarkColors else LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}