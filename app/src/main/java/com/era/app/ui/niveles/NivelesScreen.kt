package com.era.app.ui.niveles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.R
import com.era.app.data.model.NivelConProgreso
import com.era.app.ui.theme.ColorNivelBloqueado
import com.era.app.ui.theme.ColorNivelBloqueadoBorde
import com.era.app.ui.theme.ColorNivelBloqueadoFondo
import com.era.app.ui.theme.ColorNivelCompletado
import com.era.app.ui.theme.ColorNivelCompletadoFondo
import com.era.app.ui.theme.ColorNivelDisponible
import com.era.app.ui.theme.ColorNivelDisponibleFondo
import com.era.app.ui.theme.ColorSettingsBackIcon
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme

@Composable
fun NivelesScreen(
    uiState: NivelesUiState,
    onVolver: () -> Unit,
    onNivelClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("pantalla_niveles"),
    ) {
        CabeceraNiveles(onVolver = onVolver)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("lista_niveles"),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(uiState.niveles, key = { it.orden }) { nivel ->
                NivelCard(
                    nivel = nivel,
                    onNivelClick = onNivelClick,
                )
            }
        }
    }
}

@Composable
private fun CabeceraNiveles(onVolver: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.img_hero_menu_niveles),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 20.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(ColorTextWhite)
                .testTag("boton_volver")
                .clickable(onClick = onVolver),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = ColorSettingsBackIcon,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = "Trivia primaria",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextWhite,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .testTag("titulo_cabecera"),
        )
    }
}

@Composable
private fun NivelCard(
    nivel: NivelConProgreso,
    onNivelClick: (Int) -> Unit,
) {
    val bloqueado = nivel.estado == "BLOQUEADO"
    val colorFondo = when (nivel.estado) {
        "COMPLETADO" -> ColorNivelCompletadoFondo
        "DISPONIBLE" -> ColorNivelDisponibleFondo
        else -> ColorNivelBloqueadoFondo
    }
    val colorBorde = when (nivel.estado) {
        "COMPLETADO" -> ColorNivelCompletado
        "DISPONIBLE" -> ColorNivelDisponible
        else -> ColorNivelBloqueadoBorde
    }
    val colorCirculo = when (nivel.estado) {
        "COMPLETADO" -> ColorNivelCompletado
        "DISPONIBLE" -> ColorNivelDisponible
        else -> ColorNivelBloqueado
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(colorFondo)
            .border(1.5.dp, colorBorde, RoundedCornerShape(24.dp))
            .then(
                if (bloqueado) {
                    Modifier
                } else {
                    Modifier.clickable { onNivelClick(nivel.orden) }
                },
            )
            .padding(horizontal = 20.dp)
            .testTag("nivel_card_${nivel.orden}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colorCirculo),
            contentAlignment = Alignment.Center,
        ) {
            when (nivel.estado) {
                "COMPLETADO" -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ColorTextWhite,
                    modifier = Modifier.size(24.dp),
                )
                "DISPONIBLE" -> Text(
                    text = "?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextWhite,
                )
                else -> Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ColorTextWhite,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Nivel ${nivel.orden}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextDark,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = etiquetaEstado(nivel.estado),
                fontSize = 14.sp,
                color = ColorTextMuted,
            )
        }
    }
}

private fun etiquetaEstado(estado: String): String = when (estado) {
    "COMPLETADO" -> "Completado"
    "DISPONIBLE" -> "Disponible"
    else -> "Bloqueado"
}

@Preview(showBackground = true, name = "Niveles")
@Composable
private fun NivelesPreview() {
    ERATheme {
        NivelesScreen(
            uiState = NivelesUiState(
                niveles = listOf(
                    nivelPreview(1, "COMPLETADO"),
                    nivelPreview(2, "DISPONIBLE"),
                    nivelPreview(3, "BLOQUEADO"),
                ),
                cargando = false,
            ),
            onVolver = {},
            onNivelClick = {},
        )
    }
}

private fun nivelPreview(orden: Int, estado: String) = NivelConProgreso(
    orden = orden,
    pregunta = "P$orden",
    opcionA = "A",
    opcionB = "B",
    opcionC = "C",
    respuestaCorrecta = 0,
    estado = estado,
    intentosTotales = 0,
    intentosFallidosConsecutivos = 0,
    completadoEn = null,
    sincronizado = true,
)
