package com.squishout.game.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SquishColors.Kiwi,
    onPrimary = Color.White,
    secondary = SquishColors.Strawberry,
    onSecondary = Color.White,
    tertiary = SquishColors.Lemon,
    onTertiary = SquishColors.TextPrimary,
    background = SquishColors.BackgroundMint,
    onBackground = SquishColors.TextPrimary,
    surface = Color.White,
    onSurface = SquishColors.TextPrimary
)

@Composable
fun SquishOutTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = SquishTypography,
        content = content
    )
}
