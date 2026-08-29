package com.era.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.R
import com.era.app.ui.theme.BodyBase
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme
import com.era.app.ui.theme.HeroTitle

@Composable
fun HeroLogin(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier,
    ) {
        val headerHeight = (maxHeight * 0.32f).coerceAtMost(300.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(ColorPrimary),
        ) {
            Image(
                painter = painterResource(R.drawable.signo_igual),
                contentDescription = null,
                modifier = Modifier
                    .width(150.dp)
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 24.dp)
                    .graphicsLayer { translationX = 0f; translationY = 0f },
            )

            Image(
                painter = painterResource(R.drawable.signo_abc123),
                contentDescription = null,
                modifier = Modifier
                    .width(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-90).dp)
                    .padding(top = 24.dp, end = 16.dp)
                    .graphicsLayer { translationX = 0f; translationY = 0f },
            )

            Image(
                painter = painterResource(R.drawable.signomas),
                contentDescription = null,
                modifier = Modifier
                    .width(140.dp)
                    .align(Alignment.BottomEnd)
                    .offset(y = (-70).dp)
                    .padding(end = 16.dp)
                    .graphicsLayer { translationX = 0f; translationY = 0f },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 40.dp),
            ) {
                Text(
                    text = "¡Bienvenidos!",
                    style = HeroTitle.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorTextWhite,
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ERA - Educación, Repaso y Aprendizaje",
                    color = ColorTextWhite.copy(alpha = 0.85f),
                    style = BodyBase.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Hero Login")
@Composable
private fun HeroLoginPreview() {
    ERATheme {
        HeroLogin()
    }
}
