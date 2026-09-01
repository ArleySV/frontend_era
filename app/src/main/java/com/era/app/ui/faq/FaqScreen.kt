package com.era.app.ui.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.data.model.FaqItem
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.SettingsCard
import com.era.app.ui.components.SettingsHeader
import com.era.app.ui.theme.*
import com.era.app.utils.mensajeUsuario

@Composable
fun FaqScreen(
    vm: FaqViewModel,
    onVolver: () -> Unit,
    onSesionExpirada: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventos.collect { evento ->
            when (evento) {
                is FaqEvento.ComentarioEnviado -> {
                    snackbarHostState.showSnackbar("Comentario enviado con éxito")
                }
                is FaqEvento.Error -> {
                    snackbarHostState.showSnackbar(evento.error.mensajeUsuario())
                }
                is FaqEvento.SesionExpirada -> onSesionExpirada()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        FaqContent(
            uiState = uiState,
            onVolver = onVolver,
            onComentarioChange = vm::onComentarioChange,
            onEnviarComentario = vm::enviarComentario
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
internal fun FaqContent(
    uiState: FaqUiState,
    onVolver: () -> Unit,
    onComentarioChange: (String) -> Unit,
    onEnviarComentario: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorTextWhite)
    ) {
        SettingsHeader(
            titulo = "Ayuda y Comentarios",
            onVolver = onVolver,
            iconoVolver = Icons.AutoMirrored.Filled.ArrowBack,
            descripcionVolver = "Volver"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Preguntas Frecuentes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ColorPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.cargandoFaqs) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorPrimary)
                }
            } else if (uiState.errorFaqs != null) {
                Text(
                    text = uiState.errorFaqs.mensajeUsuario(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .testTag("errorFaqs")
                )
            } else {
                uiState.faqs.forEach { faq ->
                    FaqCard(faq = faq)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Envíanos tus comentarios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ColorPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Tu opinión nos ayuda a mejorar ERA para todos los niños.",
                style = MaterialTheme.typography.bodyMedium,
                color = ColorTextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            EraTextField(
                value = uiState.comentario,
                onValueChange = onComentarioChange,
                label = "Escribe aquí tu sugerencia...",
                singleLine = false,
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inputComentario"),
                error = uiState.errorComentario?.mensajeUsuario()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val colorContador = if (uiState.longitudComentario > 2000) ColorError else ColorTextMuted
                Text(
                    text = "${uiState.longitudComentario}/2000",
                    fontSize = 12.sp,
                    color = colorContador,
                    modifier = Modifier.testTag("contadorComentario")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onEnviarComentario,
                enabled = uiState.puedeEnviarComentario,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("botonEnviarComentario"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary,
                    contentColor = ColorTextWhite,
                    disabledContainerColor = Color(0xFFD5D5D5),
                    disabledContentColor = ColorTextWhite
                )
            ) {
                if (uiState.enviandoComentario) {
                    CircularProgressIndicator(
                        color = ColorTextWhite,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("cargandoEnvio")
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar comentarios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FaqCard(faq: FaqItem) {
    var expandido by remember { mutableStateOf(false) }

    SettingsCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido }
            .testTag("faqCard_${faq.id}")
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faq.pregunta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandido) "Contraer" else "Expandir",
                    tint = ColorPrimary
                )
            }

            if (expandido) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.respuesta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextMuted,
                    lineHeight = 20.sp,
                    modifier = Modifier.testTag("faqRespuesta_${faq.id}")
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FaqPreview() {
    ERATheme {
        FaqContent(
            uiState = FaqUiState(
                faqs = listOf(
                    FaqItem(1, "¿Cómo empiezo a jugar?", "Presiona el botón Trivia..."),
                    FaqItem(2, "¿Qué pasa si me equivoco?", "Tienes intentos ilimitados...")
                )
            ),
            onVolver = {},
            onComentarioChange = {},
            onEnviarComentario = {}
        )
    }
}
