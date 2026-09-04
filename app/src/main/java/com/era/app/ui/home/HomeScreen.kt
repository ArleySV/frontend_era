package com.era.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.R
import com.era.app.ui.components.EraIcons
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorSoonBg
import com.era.app.ui.theme.ColorSoonIcon
import com.era.app.ui.theme.ColorSoonTitle
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ColorTriviaBg
import com.era.app.ui.theme.ColorTriviaBtn
import com.era.app.ui.theme.ColorTriviaText
import com.era.app.ui.theme.ERATheme

@Composable
fun HomeScreen(
    nombreMenor: String,
    cargandoPerfil: Boolean,
    onOpenDrawer: () -> Unit,
    onNavegarNiveles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        HeroHome(
            nombreMenor = nombreMenor,
            cargandoPerfil = cargandoPerfil,
            onOpenDrawer = onOpenDrawer,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CardTriviaEscolar(
                onJugar = onNavegarNiveles,
                modifier = Modifier.testTag("card_trivia"),
            )
            CardProximamente(
                modifier = Modifier.testTag("card_proximamente"),
            )
        }
    }
}

@Composable
private fun HeroHome(
    nombreMenor: String,
    cargandoPerfil: Boolean,
    onOpenDrawer: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorPrimary)
            .height(300.dp),
    ) {
        // Decoración del hero: composición "signo = / ABC / 123 / +" extraída del
        // prototipo Figma (PNG con fondo transparente, en drawable-nodpi), al 76.5%
        // del ancho, anclada arriba a la derecha y desplazada 10dp a la izquierda y
        // 5dp abajo (ajuste del propietario).
        Image(
            painter = painterResource(R.drawable.img_hero_home),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 5.dp)
                .fillMaxWidth(0.765f)
                .aspectRatio(543f / 418f),
        )

        Icon(
            imageVector = EraIcons.Menu,
            contentDescription = "Abrir menú",
            tint = ColorTextWhite,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 20.dp)
                .size(40.dp)
                .testTag("boton_hamburguesa")
                .clickable(onClick = onOpenDrawer),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 24.dp)
                .widthIn(max = 250.dp),
        ) {
            Text(
                text = saludo(nombreMenor, cargandoPerfil),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextWhite,
                modifier = Modifier.testTag("saludo_home"),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nos alegra tenerte de nuevo por aquí",
                fontSize = 20.sp,
                color = ColorTextWhite,
                modifier = Modifier.width(220.dp),
            )
        }
    }
}

@Composable
private fun CardTriviaEscolar(
    onJugar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ColorTriviaBg)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = EraIcons.Puzzle,
            contentDescription = null,
            tint = ColorPrimary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Trivia Escolar",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTriviaText,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Cultura general - Nivel primaria",
            fontSize = 16.sp,
            color = ColorTriviaText,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(200.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onJugar,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorTriviaBtn,
                contentColor = ColorTextWhite,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 32.dp,
            ),
            modifier = Modifier
                .testTag("boton_jugar")
                .height(44.dp),
        ) {
            Text(
                text = "Jugar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CardProximamente(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ColorSoonBg)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = EraIcons.Clock,
            contentDescription = null,
            tint = ColorSoonIcon,
            modifier = Modifier.size(52.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Próximamente",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ColorSoonTitle,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Nuevo modo de juego",
            fontSize = 16.sp,
            color = ColorSoonIcon,
        )
    }
}

private fun saludo(nombreMenor: String, cargandoPerfil: Boolean): String {
    if (cargandoPerfil) return "¡Hola!"
    return if (nombreMenor.isBlank()) "¡Bienvenido!" else "¡Hola, $nombreMenor!"
}

@Preview(showBackground = true, name = "Home")
@Composable
private fun HomePreview() {
    ERATheme {
        HomeScreen(
            nombreMenor = "Arley",
            cargandoPerfil = false,
            onOpenDrawer = {},
            onNavegarNiveles = {},
        )
    }
}
