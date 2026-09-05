package com.era.app.ui.juego

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.R
import com.era.app.data.model.NivelConProgreso
import com.era.app.ui.theme.ColorCorrecta
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorIncorrecta
import com.era.app.ui.theme.ColorNivelBloqueado
import com.era.app.ui.theme.ColorNivelCompletado
import com.era.app.ui.theme.ColorPrimaryDark
import com.era.app.ui.theme.ColorQuizBgBottom
import com.era.app.ui.theme.ColorQuizBgTop
import com.era.app.ui.theme.ColorSurface
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ColorVerdeClaro
import com.era.app.ui.theme.ERATheme
import java.util.Locale

@Composable
fun JuegoScreen(
    uiState: JuegoUiState,
    onOpcionClick: (Int) -> Unit,
    onAbrirMenu: () -> Unit,
    onContinuar: () -> Unit,
    onReiniciar: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ColorQuizBgTop, ColorQuizBgBottom)))
            .testTag("pantalla_juego"),
    ) {
        val nivel = uiState.nivel
        if (nivel != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                CabeceraQuiz(
                    orden = nivel.orden,
                    segundos = uiState.segundosRestantes,
                    onAbrirMenu = onAbrirMenu,
                )
                CuerpoQuiz(
                    uiState = uiState,
                    nivel = nivel,
                    onOpcionClick = onOpcionClick,
                    modifier = Modifier.weight(1f),
                )
            }

            if (uiState.fase == FaseJuego.RESULTADO) {
                ResultadoSheet(
                    correcto = uiState.resultadoCorrecto == true,
                    mensaje = uiState.mensajeResultado,
                )
            }
            if (uiState.fase == FaseJuego.MENU) {
                OverlayMenuNivel(
                    orden = nivel.orden,
                    onContinuar = onContinuar,
                    onReiniciar = onReiniciar,
                    onSalir = onSalir,
                )
            }
            if (uiState.fase == FaseJuego.PAUSA) {
                OverlayPausa(
                    segundos = uiState.segundosPausa,
                    fraseSabia = uiState.fraseSabia,
                )
            }
        }
    }
}

@Composable
private fun CabeceraQuiz(
    orden: Int,
    segundos: Int,
    onAbrirMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.5.dp, ColorTextWhite.copy(alpha = 0.6f), CircleShape)
                .testTag("boton_menu_nivel")
                .clickable(onClick = onAbrirMenu),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Menú del nivel",
                tint = ColorTextWhite,
                modifier = Modifier.size(22.dp),
            )
        }
        CronometroCircular(segundos = segundos)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(ColorTextWhite)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("badge_nivel"),
        ) {
            Text(
                text = "Nivel $orden",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPrimaryDark,
            )
        }
    }
}

@Composable
private fun CronometroCircular(segundos: Int) {
    val colorAnillo = if (segundos <= 3) ColorIncorrecta else ColorTextWhite
    Box(
        modifier = Modifier
            .size(56.dp)
            .testTag("cronometro_circular"),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 4.dp.toPx())
            val inset = stroke.width / 2
            drawArc(
                color = ColorTextWhite.copy(alpha = 0.25f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke.width, size.height - stroke.width),
                style = stroke,
            )
            drawArc(
                color = colorAnillo,
                startAngle = -90f,
                sweepAngle = -360f * (segundos.coerceIn(0, 15) / 15f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke.width, size.height - stroke.width),
                style = stroke,
            )
        }
        Text(
            text = "$segundos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextWhite,
        )
    }
}

@Composable
private fun CuerpoQuiz(
    uiState: JuegoUiState,
    nivel: NivelConProgreso,
    onOpcionClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Image(
            painter = painterResource(imagenNivel(nivel.orden)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nivel ${nivel.orden} de 20",
            fontSize = 14.sp,
            color = ColorTextWhite.copy(alpha = 0.85f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = nivel.pregunta,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextWhite,
            lineHeight = 34.sp,
        )
        Spacer(modifier = Modifier.height(24.dp))
        val opciones = listOf(nivel.opcionA, nivel.opcionB, nivel.opcionC)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            opciones.forEachIndexed { indice, texto ->
                OpcionRespuesta(
                    texto = texto,
                    indice = indice,
                    uiState = uiState,
                    respuestaCorrecta = nivel.respuestaCorrecta,
                    onOpcionClick = onOpcionClick,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OpcionRespuesta(
    texto: String,
    indice: Int,
    uiState: JuegoUiState,
    respuestaCorrecta: Int,
    onOpcionClick: (Int) -> Unit,
) {
    val respondida = uiState.opcionSeleccionada != null
    val esCorrecto = uiState.resultadoCorrecto == true
    val esMiRespuesta = respondida && indice == uiState.opcionSeleccionada
    val esRespuestaCorrecta = indice == respuestaCorrecta

    val fondo: Color
    val contenido: Color
    when {
        respondida && esCorrecto && esRespuestaCorrecta -> {
            fondo = ColorCorrecta; contenido = ColorTextWhite
        }
        respondida && esMiRespuesta && !esCorrecto -> {
            fondo = ColorIncorrecta; contenido = ColorTextWhite
        }
        else -> {
            fondo = ColorTextWhite; contenido = ColorTextDark
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(fondo)
            .then(if (respondida) Modifier else Modifier.clickable { onOpcionClick(indice) })
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("opcion_$indice"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = texto.uppercase(Locale.getDefault()),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = contenido,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultadoSheet(correcto: Boolean, mensaje: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(ColorTextWhite)
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 32.dp)
                .testTag("resultado_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorTextMuted.copy(alpha = 0.4f)),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = mensaje,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (correcto) ColorCorrecta else ColorIncorrecta,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (correcto) {
                    "¡Sigue así!"
                } else {
                    "¡No te rindas!"
                },
                fontSize = 16.sp,
                color = ColorTextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OverlayMenuNivel(
    orden: Int,
    onContinuar: () -> Unit,
    onReiniciar: () -> Unit,
    onSalir: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .testTag("overlay_menu_nivel"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ColorSurface)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(imagenNivel(orden)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp, max = 160.dp)
                    .clip(RoundedCornerShape(14.dp)),
            )
            Spacer(modifier = Modifier.height(4.dp))
            BotonMenu("Continuar", ColorNivelCompletado, onContinuar, "menu_continuar")
            BotonMenu("Reiniciar", ColorNivelBloqueado, onReiniciar, "menu_reiniciar")
            BotonMenu("Salir", ColorError, onSalir, "menu_salir")
        }
    }
}

@Composable
private fun BotonMenu(
    texto: String,
    colorTexto: Color,
    onClick: () -> Unit,
    tag: String,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorTextWhite,
            contentColor = colorTexto,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .testTag(tag),
    ) {
        Text(texto, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OverlayPausa(segundos: Int, fraseSabia: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0023333))
            .testTag("overlay_pausa"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🧘", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Estírate y respira.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextWhite,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(ColorVerdeClaro, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    text = fraseSabia,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorTextDark,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .border(3.dp, ColorTextWhite, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$segundos",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextWhite,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tómate un momento.",
                fontSize = 16.sp,
                color = ColorTextWhite.copy(alpha = 0.9f),
            )
        }
    }
}

fun imagenNivel(orden: Int): Int = when (orden) {
    1 -> R.drawable.img_study1
    2 -> R.drawable.img_study2
    3 -> R.drawable.img_study3
    4 -> R.drawable.img_study4
    5 -> R.drawable.img_study5
    6 -> R.drawable.img_study6
    7 -> R.drawable.img_study7
    8 -> R.drawable.img_study8
    9 -> R.drawable.img_study9
    10 -> R.drawable.img_study10
    11 -> R.drawable.img_study11
    12 -> R.drawable.img_study12
    13 -> R.drawable.img_study13
    14 -> R.drawable.img_study14
    15 -> R.drawable.img_study15
    16 -> R.drawable.img_study16
    17 -> R.drawable.img_study17
    18 -> R.drawable.img_study18
    19 -> R.drawable.img_study19
    else -> R.drawable.img_study20
}

@Preview(showBackground = true, name = "Juego")
@Composable
private fun JuegoPreview() {
    ERATheme {
        JuegoScreen(
            uiState = JuegoUiState(
                fase = FaseJuego.JUGANDO,
                nivel = NivelConProgreso(
                    orden = 3,
                    pregunta = "¿Cuánto es 6 × 7?",
                    opcionA = "42",
                    opcionB = "36",
                    opcionC = "49",
                    respuestaCorrecta = 0,
                    estado = "DISPONIBLE",
                    intentosTotales = 0,
                    intentosFallidosConsecutivos = 0,
                    completadoEn = null,
                    sincronizado = true,
                ),
                segundosRestantes = 14,
            ),
            onOpcionClick = {},
            onAbrirMenu = {},
            onContinuar = {},
            onReiniciar = {},
            onSalir = {},
        )
    }
}