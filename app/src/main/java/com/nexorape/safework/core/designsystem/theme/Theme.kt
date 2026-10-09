package com.nexorape.safework.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF5A5CA0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E4FF),
    onPrimaryContainer = Color(0xFF292A60),
    secondary = Color(0xFF5A5CA0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEFA),
    onSecondaryContainer = Color(0xFF383A70),
    tertiary = Color(0xFF3D6858),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDAF2E7),
    onTertiaryContainer = Color(0xFF173F30),
    background = Color(0xFFF7F7FC),
    onBackground = Color(0xFF1D1C34),
    surface = Color.White,
    onSurface = Color(0xFF1D1C34),
    surfaceVariant = Color(0xFFEFEEF7),
    onSurfaceVariant = Color(0xFF59586F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F7FC),
    surfaceContainer = Color(0xFFEFEEF7),
    surfaceContainerHigh = Color(0xFFE9E8F3),
    surfaceContainerHighest = Color(0xFFE3E2EE),
    outline = Color(0xFF7B798C),
    outlineVariant = Color(0xFFDAD9E8),
    error = Color(0xFFA72A3D),
    onError = Color.White,
    errorContainer = Color(0xFFFFE9ED),
    onErrorContainer = Color(0xFF76162B),
    inverseSurface = Color(0xFF302F45),
    inverseOnSurface = Color(0xFFF5F3FC),
    inversePrimary = Color(0xFFBDBFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBDBFFF),
    onPrimary = Color(0xFF24244E),
    primaryContainer = Color(0xFF383968),
    onPrimaryContainer = Color(0xFFE5E4FF),
    secondary = Color(0xFFC1BFE8),
    onSecondary = Color(0xFF2F2D50),
    secondaryContainer = Color(0xFF302F52),
    onSecondaryContainer = Color(0xFFE5E2FF),
    tertiary = Color(0xFF9DD5BE),
    onTertiary = Color(0xFF143D2D),
    tertiaryContainer = Color(0xFF234A3A),
    onTertiaryContainer = Color(0xFFDAF2E7),
    background = Color(0xFF0D0C22),
    onBackground = Color(0xFFF4F2FF),
    surface = Color(0xFF18172F),
    onSurface = Color(0xFFF4F2FF),
    surfaceVariant = Color(0xFF29283F),
    onSurfaceVariant = Color(0xFFC2C0D4),
    surfaceContainerLowest = Color(0xFF0D0C22),
    surfaceContainerLow = Color(0xFF18172F),
    surfaceContainer = Color(0xFF201F37),
    surfaceContainerHigh = Color(0xFF29283F),
    surfaceContainerHighest = Color(0xFF33324A),
    outline = Color(0xFF9693AC),
    outlineVariant = Color(0xFF45435D),
    error = Color(0xFFFFB1BF),
    onError = Color(0xFF620F26),
    errorContainer = Color(0xFF512030),
    onErrorContainer = Color(0xFFFFD9E0),
    inverseSurface = Color(0xFFE5E2F3),
    inverseOnSurface = Color(0xFF302F45),
    inversePrimary = Color(0xFF5A5CA0),
)

/** Original brand violet is an accent; text/action pairings use contrast-adjusted tones. */
val SafeWorkBrandViolet = Color(0xFF7B7DC1)

private val SafeWorkShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SafeWorkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SafeWorkTypography,
        shapes = SafeWorkShapes,
        content = content,
    )
}
