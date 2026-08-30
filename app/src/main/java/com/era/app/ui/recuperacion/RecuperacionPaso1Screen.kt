package com.era.app.ui.recuperacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.sp
import com.era.app.ui.components.CompactGreenHeader
import com.era.app.ui.components.EraRegPrimaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.StepIndicator
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.EraError
import com.era.app.utils.mensaje
import com.era.app.utils.mensajeUsuario
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RecuperacionPaso1Screen(
    vm: RecuperacionViewModel,
    snackbarHostState: SnackbarHostState,
    onVolverAlLogin: () -> Unit,
    onNavegarAPaso2: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventos.collectLatest { evento ->
            when (evento) {
                is RecuperacionEvento.NavegarAPaso2 -> onNavegarAPaso2()
                is RecuperacionEvento.Aviso -> {
                    snackbarHostState.showSnackbar(evento.error.mensajeUsuario())
                }
                else -> {}
            }
        }
    }

    RecuperacionPaso1Content(
        correo = state.correo,
        onCorreoChange = vm::onCorreoChange,
        correoError = CampoRecuperacion.CORREO.mensaje(state.errores, state.errorGeneral),
        errorGeneral = state.errorGeneral,
        onEnviarCodigo = vm::enviarEnlace,
        onVolverAlLogin = onVolverAlLogin,
    )
}

@Composable
internal fun RecuperacionPaso1Content(
    correo: String,
    onCorreoChange: (String) -> Unit,
    correoError: String?,
    errorGeneral: EraError?,
    onEnviarCodigo: () -> Unit,
    onVolverAlLogin: () -> Unit,
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
            StepIndicator(pasoActual = 1, modifier = Modifier.padding(vertical = 16.dp))

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

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ingresa el correo de tu cuenta. Te enviaremos un código de verificación para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                errorGeneral?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error.mensajeUsuario(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorError,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                EraTextField(
                    value = correo,
                    onValueChange = onCorreoChange,
                    label = "Correo electrónico",
                    obligatorio = true,
                    placeholder = "correo@ejemplo.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    error = correoError,
                    modifier = Modifier.testTag("campoCorreo"),
                )

                Spacer(modifier = Modifier.height(20.dp))

                EraRegPrimaryButton(
                    texto = "Enviar código",
                    onClick = onEnviarCodigo,
                    icono = null,
                    modifier = Modifier.testTag("botonEnviarCodigo"),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Volver al inicio de sesión",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onVolverAlLogin() }
                        .padding(8.dp)
                        .testTag("linkVolverAlLogin"),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Recuperar contraseña paso 1")
@Composable
private fun RecuperacionPaso1Preview() {
    ERATheme {
        RecuperacionPaso1Content(
            correo = "",
            onCorreoChange = {},
            correoError = null,
            errorGeneral = null,
            onEnviarCodigo = {},
            onVolverAlLogin = {},
        )
    }
}