package com.era.app.ui.register

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.components.CompactGreenHeader
import com.era.app.ui.components.EraRegPrimaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.InfoBox
import com.era.app.ui.components.StepIndicator
import com.era.app.ui.theme.ColorOtpIconBg
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.EraError
import com.era.app.utils.mensaje
import com.era.app.utils.mensajeUsuario
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegistroPaso3Screen(
    vm: RegistroViewModel,
    snackbarHostState: SnackbarHostState,
    onRegistroExitoso: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventos.collectLatest { evento ->
            when (evento) {
                is RegistroEvento.RegistroVerificadoIrALogin -> onRegistroExitoso()
                is RegistroEvento.Aviso -> {
                    snackbarHostState.showSnackbar(evento.error.mensajeUsuario())
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(state.errorGeneral) {
        val err = state.errorGeneral
        if (err is EraError.Validacion) {
            snackbarHostState.showSnackbar(err.mensajeUsuario())
        }
    }

    val countdownText = if (state.reenvioSegundosRestantes > 0) {
        "Reenviar código (${state.reenvioSegundosRestantes}s)"
    } else {
        "Reenviar código"
    }

    RegistroPaso3Content(
        correo = state.correo,
        codigoOtp = state.codigoOtp,
        onCodigoOtpChange = vm::onCodigoOtpChange,
        codigoOtpError = CampoRegistro.CODIGO_OTP.mensaje(state.errores, state.errorGeneral),
        countdownHabilitado = state.reenvioSegundosRestantes == 0,
        countdownTexto = countdownText,
        onReenviarCodigo = vm::reenviarCodigo,
        onVerificarCodigo = vm::verificarCodigo,
    )
}

@Composable
internal fun RegistroPaso3Content(
    correo: String,
    codigoOtp: String,
    onCodigoOtpChange: (String) -> Unit,
    codigoOtpError: String?,
    countdownHabilitado: Boolean,
    countdownTexto: String,
    onReenviarCodigo: () -> Unit,
    onVerificarCodigo: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            CompactGreenHeader(
                titulo = "Registro - Paso 3 de 3",
                subtitulo = "Verifica tu correo",
            )
            StepIndicator(pasoActual = 3, modifier = Modifier.padding(vertical = 16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(122.dp)
                        .clip(CircleShape)
                        .background(ColorOtpIconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = ColorTextWhite,
                        modifier = Modifier.size(60.dp),
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

                EraTextField(
                    value = codigoOtp,
                    onValueChange = onCodigoOtpChange,
                    label = "Código de verificación",
                    obligatorio = true,
                    placeholder = "000000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    error = codigoOtpError,
                )

                Spacer(modifier = Modifier.height(20.dp))

                EraRegPrimaryButton(
                    texto = "Verificar código",
                    onClick = onVerificarCodigo,
                    icono = null,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = countdownTexto,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (countdownHabilitado) ColorPrimary else ColorTextMuted,
                    fontWeight = if (countdownHabilitado) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable(enabled = countdownHabilitado) { onReenviarCodigo() }
                        .padding(8.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                InfoBox(
                    texto = "El código expira en 10 minutos. Si no lo recibes, revisa spam o reenvíalo",
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Paso 3 registro")
@Composable
private fun RegistroPaso3Preview() {
    ERATheme {
        RegistroPaso3Content(
            correo = "correo@ejemplo.com",
            codigoOtp = "",
            onCodigoOtpChange = {},
            codigoOtpError = null,
            countdownHabilitado = true,
            countdownTexto = "Reenviar código",
            onReenviarCodigo = {},
            onVerificarCodigo = {},
        )
    }
}
