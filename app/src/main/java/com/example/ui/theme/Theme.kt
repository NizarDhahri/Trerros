package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WillowlightColorScheme = lightColorScheme(
    primary = CozyMint,
    onPrimary = CozyCream,
    primaryContainer = LanternGlow,
    onPrimaryContainer = InkEspresso,
    secondary = MeadowSage,
    onSecondary = CozyCream,
    secondaryContainer = ParchmentBase,
    onSecondaryContainer = InkEspresso,
    tertiary = LanternAmber,
    onTertiary = CozyCream,
    tertiaryContainer = WildflowerPink,
    onTertiaryContainer = InkEspresso,
    background = ParchmentLight,
    onBackground = InkEspresso,
    surface = CozyCream,
    onSurface = InkEspresso,
    surfaceVariant = ParchmentBase,
    onSurfaceVariant = InkSoft,
    outline = CozyCreamBorder,
    outlineVariant = ParchmentShadow
)

val WillowlightShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun WillowlightTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WillowlightColorScheme,
        typography = WillowlightTypography,
        shapes = WillowlightShapes,
        content = content
    )
}
