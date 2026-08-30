package com.era.app.ui.recuperacion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.ui.components.CompactGreenHeader
import com.era.app.ui.components.EraIcons
import com.era.app.ui.components.EraRegPrimaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.InfoBox
import com.era.app.ui.components.StepIndicator
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextDark
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.CriteriosContrasena
import com.era.app.utils.EraError
import com.era.app.utils.mensaje
import com.era.app.utils.mensajeUsuario
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RecuperacionPaso3Screen(
    vm: RecuperacionViewModel,
    snackbarHostState: SnackbarHostState,
    onReiniciarFlujo: () -> Unit,
    onRecuperacionExitosa: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventos.collectLatest { evento ->
            when (evento) {
                is RecuperacionEvento.ReiniciarFlujo -> onReiniciarFlujo()
                is RecuperacionEvento.RecuperacionExitosa -> onRecuperacionExitosa()
                is RecuperacionEvento.Aviso -> {
                    snackbarHostState.showSnackbar(evento.error.mensajeUsuario())
                }
                else -> {}
            }
        }
    }

    RecuperacionPaso3Content(
        nuevaContrasena = state.nuevaContrasena,
        onNuevaContrasenaChange = vm::onNuevaContrasenaChange,
        nuevaContrasenaError = CampoRecuperacion.NUEVA_CONTRASENA.mensaje(state.errores, state.errorGeneral),
        nuevaContrasenaVisible = state.nuevaContrasenaVisible,
        onNuevaContrasenaVisibleToggle = vm::toggleNuevaContrasenaVisible,
        confirmarContrasena = state.confirmarContrasena,
        onConfirmarContrasenaChange = vm::onConfirmarContrasenaChange,
        confirmarError = CampoRecuperacion.CONFIRMAR_CONTRASENA.mensaje(state.errores, state.errorGeneral),
        confirmarVisible = state.confirmarVisible,
        onConfirmarVisibleToggle = vm::toggleConfirmarVisible,
        criteriosContrasena = state.criteriosContrasena,
        errorGeneral = state.errorGeneral,
        onGuardarContrasena = vm::guardarContrasena,
    )
}

@Composable
internal fun RecuperacionPaso3Content(
    nuevaContrasena: String,
    onNuevaContrasenaChange: (String) -> Unit,
    nuevaContrasenaError: String?,
    nuevaContrasenaVisible: Boolean,
    onNuevaContrasenaVisibleToggle: () -> Unit,
    confirmarContrasena: String,
    onConfirmarContrasenaChange: (String) -> Unit,
    confirmarError: String?,
    confirmarVisible: Boolean,
    onConfirmarVisibleToggle: () -> Unit,
    criteriosContrasena: CriteriosContrasena,
    errorGeneral: EraError?,
    onGuardarContrasena: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            CompactGreenHeader(
                titulo = "Recuperar contraseña",
                subtitulo = "Elige una nueva contraseña",
            )
            StepIndicator(pasoActual = 3, modifier = Modifier.padding(vertical = 16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
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
                    value = nuevaContrasena,
                    onValueChange = onNuevaContrasenaChange,
                    label = "Nueva contraseña",
                    obligatorio = true,
                    error = nuevaContrasenaError,
                    iconoFin = if (nuevaContrasenaVisible) EraIcons.VisibilityOff else EraIcons.Visibility,
                    descripcionIconoFin = if (nuevaContrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    onIconoFinClick = onNuevaContrasenaVisibleToggle,
                    visualTransformation = if (nuevaContrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.testTag("campoNuevaContrasena"),
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mín. 8 caracteres, mayúscula, minúscula, número y símbolo",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextMuted,
                )

                Spacer(modifier = Modifier.height(12.dp))

                CriteriosContrasenaRow("Mín. 8 caracteres", criteriosContrasena.longitudMinima)
                CriteriosContrasenaRow("Una letra mayúscula", criteriosContrasena.tieneMayuscula)
                CriteriosContrasenaRow("Una letra minúscula", criteriosContrasena.tieneMinuscula)
                CriteriosContrasenaRow("Un número", criteriosContrasena.tieneNumero)
                CriteriosContrasenaRow("Un símbolo", criteriosContrasena.tieneSimbolo)

                Spacer(modifier = Modifier.height(12.dp))

                EraTextField(
                    value = confirmarContrasena,
                    onValueChange = onConfirmarContrasenaChange,
                    label = "Confirmar contraseña",
                    obligatorio = true,
                    error = confirmarError,
                    iconoFin = if (confirmarVisible) EraIcons.VisibilityOff else EraIcons.Visibility,
                    descripcionIconoFin = if (confirmarVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    onIconoFinClick = onConfirmarVisibleToggle,
                    visualTransformation = if (confirmarVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.testTag("campoConfirmarContrasena"),
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoBox(
                    texto = "La contraseña no puede contener tu nombre, ser una palabra del diccionario ni ser igual al usuario",
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                EraRegPrimaryButton(
                    texto = "Guardar contraseña",
                    onClick = onGuardarContrasena,
                    icono = null,
                    modifier = Modifier.testTag("botonGuardarContrasena"),
                )
            }
        }
    }
}

@Composable
private fun CriteriosContrasenaRow(texto: String, cumplido: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (cumplido) "✓" else "○",
            fontSize = 14.sp,
            color = if (cumplido) ColorPrimary else ColorTextMuted,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            fontSize = 14.sp,
            color = if (cumplido) ColorTextDark else ColorTextMuted,
        )
    }
}

@Preview(showBackground = true, name = "Recuperar contraseña paso 3")
@Composable
private fun RecuperacionPaso3Preview() {
    ERATheme {
        RecuperacionPaso3Content(
            nuevaContrasena = "",
            onNuevaContrasenaChange = {},
            nuevaContrasenaError = null,
            nuevaContrasenaVisible = false,
            onNuevaContrasenaVisibleToggle = {},
            confirmarContrasena = "",
            onConfirmarContrasenaChange = {},
            confirmarError = null,
            confirmarVisible = false,
            onConfirmarVisibleToggle = {},
            criteriosContrasena = CriteriosContrasena(
                longitudMinima = false,
                tieneMayuscula = false,
                tieneMinuscula = false,
                tieneNumero = false,
                tieneSimbolo = false,
                distintaDeUsuario = true,
                sinDatosPersonales = true,
            ),
            errorGeneral = null,
            onGuardarContrasena = {},
        )
    }
}