package com.era.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val HeroTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Light,
    fontSize = 32.sp
)

val FormTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp
)

val HeroRegTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp
)

val BodyBase = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)

val TextSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
)

val TextXSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp
)

val Typography = Typography(
    displaySmall = FormTitle,
    headlineMedium = HeroTitle,
    titleMedium = HeroRegTitle,
    bodyLarge = BodyBase,
    bodyMedium = TextSmall,
    labelMedium = TextXSmall
)
