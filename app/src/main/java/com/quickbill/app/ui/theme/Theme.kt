package com.quickbill.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Blue = Color(0xFF126FEF)
val BlueDark = Color(0xFF0B56C5)
val BlueContainer = Color(0xFFE5F0FF)
val Background = Color(0xFFF3F6FB)
val Surface = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF172033)
val TextSecondary = Color(0xFF5B6478)
val Line = Color(0xFFE1E6EE)
val WhatsAppGreen = Color(0xFF159447)
val WhatsAppGreenContainer = Color(0xFFE3F5EA)
val ErrorRed = Color(0xFFC43D3D)
val ErrorRedContainer = Color(0xFFFBEAEA)

private val QuickBillColors = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Blue,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppGreen,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Background,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    outline = Line,
    surfaceTint = Color.Transparent
)

private val QuickBillTypography = Typography(
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    bodyLarge = TextStyle(fontSize = 17.sp, color = TextPrimary),
    bodyMedium = TextStyle(fontSize = 16.sp, color = TextPrimary),
    bodySmall = TextStyle(fontSize = 14.sp, color = TextSecondary),
    labelLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun QuickBillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QuickBillColors,
        typography = QuickBillTypography,
        content = content
    )
}
