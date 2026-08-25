package com.era.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val RadiusPill = 25.5.dp
val RadiusPanel = 30.dp
val RadiusInputReg = 10.dp
val RadiusBtnReg = 8.dp
val RadiusCard = 30.dp
val RadiusInfoBox = 6.dp

private val EraShapes = Shapes(
    extraSmall = RoundedCornerShape(RadiusBtnReg),
    small = RoundedCornerShape(RadiusBtnReg),
    medium = RoundedCornerShape(RadiusInputReg),
    large = RoundedCornerShape(RadiusPill),
    extraLarge = RoundedCornerShape(RadiusPanel)
)

private val EraColorScheme = lightColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorTextWhite,
    primaryContainer = ColorPrimaryPale,
    onPrimaryContainer = ColorTextDark,
    inversePrimary = ColorPrimaryDark,
    secondary = ColorPrimaryLight,
    onSecondary = ColorTextWhite,
    secondaryContainer = ColorPrimaryPale,
    onSecondaryContainer = ColorTextDark,
    tertiary = ColorTriviaBtn,
    onTertiary = ColorTextWhite,
    background = ColorSurfaceWhite,
    onBackground = ColorTextDark,
    surface = ColorSurfaceWhite,
    onSurface = ColorTextDark,
    surfaceVariant = ColorSurface,
    onSurfaceVariant = ColorTextBody,
    error = ColorError,
    onError = ColorTextWhite,
    outline = ColorTextMuted
)

@Composable
fun ERATheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EraColorScheme,
        typography = Typography,
        shapes = EraShapes,
        content = content
    )
}
