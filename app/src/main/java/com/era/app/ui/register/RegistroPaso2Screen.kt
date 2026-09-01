package com.era.app.ui.register

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.components.CompactGreenHeader
import com.era.app.ui.components.EraIcons
import com.era.app.ui.components.EraRegPrimaryButton
import com.era.app.ui.components.EraRegSecondaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.InfoBox
import com.era.app.ui.components.StepIndicator
import com.era.app.ui.components.avatar.AvatarSelector
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.CriteriosContrasena
import com.era.app.utils.EraError
import com.era.app.utils.Validators
import com.era.app.utils.mensaje
import com.era.app.utils.mensajeUsuario
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegistroPaso2Screen(
    vm: RegistroViewModel,
    snackbarHostState: SnackbarHostState,
    onAtras: () -> Unit,
    onNavegarAPaso3: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventos.collectLatest { evento ->
            when (evento) {
                is RegistroEvento.NavegarAPaso3 -> onNavegarAPaso3()
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

    var contrasenaVisible by remember { mutableStateOf(false) }
    var confirmarVisible by remember { mutableStateOf(false) }

    RegistroPaso2Content(
        correo = state.correo,
        onCorreoChange = vm::onCorreoChange,
        correoError = CampoRegistro.CORREO.mensaje(state.errores, state.errorGeneral),
        nombreUsuario = state.nombreUsuario,
        onNombreUsuarioChange = vm::onNombreUsuarioChange,
        nombreUsuarioError = CampoRegistro.NOMBRE_USUARIO.mensaje(state.errores, state.errorGeneral),
        avatarSeleccionado = state.avatarSeleccionado,
        onAvatarSeleccionar = vm::onAvatarSeleccionar,
        avatarError = CampoRegistro.AVATAR.mensaje(state.errores, state.errorGeneral),
        contrasena = state.contrasena,
        onContrasenaChange = vm::onContrasenaChange,
        contrasenaError = CampoRegistro.CONTRASENA.mensaje(state.errores, state.errorGeneral),
        contrasenaVisible = contrasenaVisible,
        onContrasenaVisibleChange = { contrasenaVisible = it },
        confirmarContrasena = state.confirmarContrasena,
        onConfirmarContrasenaChange = vm::onConfirmarContrasenaChange,
        confirmarError = CampoRegistro.CONFIRMAR_CONTRASENA.mensaje(state.errores, state.errorGeneral),
        confirmarVisible = confirmarVisible,
        onConfirmarVisibleChange = { confirmarVisible = it },
        criteriosContrasena = state.criteriosContrasena,
        onAtras = onAtras,
        onContinuar = vm::continuarPaso2,
    )
}

@Composable
internal fun RegistroPaso2Content(
    correo: String,
    onCorreoChange: (String) -> Unit,
    correoError: String?,
    nombreUsuario: String,
    onNombreUsuarioChange: (String) -> Unit,
    nombreUsuarioError: String?,
    avatarSeleccionado: Int?,
    onAvatarSeleccionar: (Int) -> Unit,
    avatarError: String?,
    contrasena: String,
    onContrasenaChange: (String) -> Unit,
    contrasenaError: String?,
    contrasenaVisible: Boolean,
    onContrasenaVisibleChange: (Boolean) -> Unit,
    confirmarContrasena: String,
    onConfirmarContrasenaChange: (String) -> Unit,
    confirmarError: String?,
    confirmarVisible: Boolean,
    onConfirmarVisibleChange: (Boolean) -> Unit,
    criteriosContrasena: CriteriosContrasena,
    onAtras: () -> Unit,
    onContinuar: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            CompactGreenHeader(
                titulo = "Registro - Paso 2 de 3",
                subtitulo = "Configura tu cuenta",
            )
            StepIndicator(pasoActual = 2, modifier = Modifier.padding(vertical = 16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EraTextField(
                    value = correo,
                    onValueChange = onCorreoChange,
                    label = "Correo electrónico",
                    obligatorio = true,
                    placeholder = "correo@ejemplo.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    error = correoError,
                )

                Spacer(modifier = Modifier.height(12.dp))

                EraTextField(
                    value = nombreUsuario,
                    onValueChange = onNombreUsuarioChange,
                    label = "Nombre de usuario",
                    obligatorio = true,
                    placeholder = "@usuario",
                    error = nombreUsuarioError,
                    textoAyuda = "${nombreUsuario.length} / ${Validators.USERNAME_MAX_LENGTH}",
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Elige un avatar",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextMuted,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AvatarSelector(
                    seleccionado = avatarSeleccionado,
                    onSeleccionar = onAvatarSeleccionar,
                )
                if (avatarError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = avatarError,
                        style = MaterialTheme.typography.labelMedium,
                        color = com.era.app.ui.theme.ColorError,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                EraTextField(
                    value = contrasena,
                    onValueChange = onContrasenaChange,
                    label = "Contraseña",
                    obligatorio = true,
                    error = contrasenaError,
                    iconoFin = if (contrasenaVisible) EraIcons.VisibilityOff else EraIcons.Visibility,
                    descripcionIconoFin = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    onIconoFinClick = { onContrasenaVisibleChange(!contrasenaVisible) },
                    visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mín. 8 caracteres, mayúscula, minúscula, número y símbolo",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextMuted,
                )

                Spacer(modifier = Modifier.height(12.dp))

                EraTextField(
                    value = confirmarContrasena,
                    onValueChange = onConfirmarContrasenaChange,
                    label = "Confirmar contraseña",
                    obligatorio = true,
                    error = confirmarError,
                    iconoFin = if (confirmarVisible) EraIcons.VisibilityOff else EraIcons.Visibility,
                    descripcionIconoFin = if (confirmarVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    onIconoFinClick = { onConfirmarVisibleChange(!confirmarVisible) },
                    visualTransformation = if (confirmarVisible) VisualTransformation.None else PasswordVisualTransformation(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoBox(
                    texto = "La contraseña no puede contener tu nombre, ser una palabra del diccionario ni ser igual al usuario",
                    modifier = Modifier.padding(bottom = 16.dp),
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
                texto = "Continuar",
                onClick = onContinuar,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(showBackground = true, name = "Paso 2 registro")
@Composable
private fun RegistroPaso2Preview() {
    ERATheme {
        RegistroPaso2Content(
            correo = "",
            onCorreoChange = {},
            correoError = null,
            nombreUsuario = "",
            onNombreUsuarioChange = {},
            nombreUsuarioError = null,
            avatarSeleccionado = null,
            onAvatarSeleccionar = {},
            avatarError = null,
            contrasena = "",
            onContrasenaChange = {},
            contrasenaError = null,
            contrasenaVisible = false,
            onContrasenaVisibleChange = {},
            confirmarContrasena = "",
            onConfirmarContrasenaChange = {},
            confirmarError = null,
            confirmarVisible = false,
            onConfirmarVisibleChange = {},
            criteriosContrasena = CriteriosContrasena(
                longitudMinima = false,
                tieneMayuscula = false,
                tieneMinuscula = false,
                tieneNumero = false,
                tieneSimbolo = false,
                distintaDeUsuario = true,
                sinDatosPersonales = true,
            ),
            onAtras = {},
            onContinuar = {},
        )
    }
}
