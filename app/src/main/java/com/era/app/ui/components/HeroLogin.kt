package com.era.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.era.app.R
import com.era.app.ui.theme.BodyBase
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme
import com.era.app.ui.theme.HeroTitle

@Composable
fun HeroLogin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(283.dp)
            .background(ColorPrimary)
            .padding(horizontal = 28.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.signo_igual),
            contentDescription = null,
            modifier = Modifier
                .width(140.dp)
                .offset(x = 0.dp, y = 0.dp)
                .graphicsLayer { translationX = 0f; translationY = 0f },
        )

        Image(
            painter = painterResource(R.drawable.signo_abc123),
            contentDescription = null,
            modifier = Modifier
                .width(170.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = 0f; translationY = 0f },
        )

        Image(
            painter = painterResource(R.drawable.signomas),
            contentDescription = null,
            modifier = Modifier
                .width(140.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer { translationX = 0f; translationY = 0f },
        )

        Text(
            text = "¡Bienvenidos!",
            style = HeroTitle.copy(color = ColorTextWhite),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 130.dp),
        )

        Text(
            text = "ERA - Educación, Repaso y Aprendizaje",
            color = ColorTextWhite.copy(alpha = 0.85f),
            style = BodyBase.copy(fontWeight = FontWeight.Light),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 178.dp)
                .width(215.dp),
        )
    }
}

@Preview(showBackground = true, name = "Hero Login")
@Composable
private fun HeroLoginPreview() {
    ERATheme {
        HeroLogin()
    }
}
