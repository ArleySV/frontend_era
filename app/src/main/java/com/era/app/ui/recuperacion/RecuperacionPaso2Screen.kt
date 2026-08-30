package com.era.app.ui.recuperacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.components.CompactGreenHeader
import com.era.app.ui.components.EraRegPrimaryButton
import com.era.app.ui.components.EraRegSecondaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.InfoBox
import com.era.app.ui.components.StepIndicator
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.EraError
import com.era.app.utils.mensaje
import com.era.app.utils.mensajeUsuario
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RecuperacionPaso2Screen(
    vm: RecuperacionViewModel,
    snackbarHostState: SnackbarHostState,
    onAtras: () -> Unit,
    onNavegarAPaso3: () -> Unit,
    onReiniciarFlujo: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventos.collectLatest { evento ->
            when (evento) {
                is RecuperacionEvento.NavegarAPaso3 -> onNavegarAPaso3()
                is RecuperacionEvento.ReiniciarFlujo -> onReiniciarFlujo()
                is RecuperacionEvento.Aviso -> {
                    snackbarHostState.showSnackbar(evento.error.mensajeUsuario())
                }
                else -> {}
            }
        }
    }

    val countdownTexto = if (state.reenvioSegundosRestantes > 0) {
        "Reenviar código (${state.reenvioSegundosRestantes}s)"
    } else {
        "Reenviar código"
    }

    RecuperacionPaso2Content(
        correo = state.correo,
        codigoOtp = state.codigoOtp,
        onCodigoOtpChange = vm::onCodigoOtpChange,
        codigoOtpError = CampoRecuperacion.CODIGO_OTP.mensaje(state.errores, state.errorGeneral),
        errorGeneral = state.errorGeneral,
        countdownHabilitado = state.reenvioSegundosRestantes == 0,
        countdownTexto = countdownTexto,
        onReenviarCodigo = vm::reenviarCodigo,
        onAtras = onAtras,
        onVerificarCodigo = vm::verificarCodigo,
    )
}

@Composable
internal fun RecuperacionPaso2Content(
    correo: String,
    codigoOtp: String,
    onCodigoOtpChange: (String) -> Unit,
    codigoOtpError: String?,
    errorGeneral: EraError?,
    countdownHabilitado: Boolean,
    countdownTexto: String,
    onReenviarCodigo: () -> Unit,
    onAtras: () -> Unit,
    onVerificarCodigo: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            CompactGreenHeader(
                titulo = "Recuperar contraseña",
                subtitulo = "Te enviamos un código de verificación a tu correo",
            )
            StepIndicator(pasoActual = 2, modifier = Modifier.padding(vertical = 16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(52.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Código enviado a",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    color = ColorTextDark,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = correo,
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorTextDark,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                errorGeneral?.let { error ->
                    Text(
                        text = error.mensajeUsuario(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorError,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                EraTextField(
                    value = codigoOtp,
                    onValueChange = onCodigoOtpChange,
                    label = "Código de verificación",
                    obligatorio = true,
                    placeholder = "000000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    error = codigoOtpError,
                    modifier = Modifier.testTag("campoCodigo"),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = countdownTexto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (countdownHabilitado) ColorPrimary else ColorTextMuted,
                    fontWeight = if (countdownHabilitado) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable(enabled = countdownHabilitado) { onReenviarCodigo() }
                        .padding(8.dp)
                        .testTag("botonReenviarCodigo"),
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoBox(
                    texto = "El código expira en 10 minutos. Si no lo recibes, revisa spam o reenvíalo",
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EraRegSecondaryButton(
                texto = "Atrás",
                onClick = onAtras,
                modifier = Modifier.weight(1f),
            )
            EraRegPrimaryButton(
                texto = "Verificar código",
                onClick = onVerificarCodigo,
                icono = null,
                modifier = Modifier
                    .weight(1f)
                    .testTag("botonVerificarCodigo"),
            )
        }
    }
}

@Preview(showBackground = true, name = "Recuperar contraseña paso 2")
@Composable
private fun RecuperacionPaso2Preview() {
    ERATheme {
        RecuperacionPaso2Content(
            correo = "correo@ejemplo.com",
            codigoOtp = "",
            onCodigoOtpChange = {},
            codigoOtpError = null,
            errorGeneral = null,
            countdownHabilitado = true,
            countdownTexto = "Reenviar código",
            onReenviarCodigo = {},
            onAtras = {},
            onVerificarCodigo = {},
        )
    }
}