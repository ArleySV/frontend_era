package com.era.app.ui.perfil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.era.app.ui.components.EraRegPrimaryButton
import com.era.app.ui.components.EraRegSecondaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.SettingsCard
import com.era.app.ui.components.SettingsHeader
import com.era.app.ui.components.EraIcons
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.mensajeUsuario

@Composable
fun EliminarCuentaScreen(
    vm: EliminarCuentaViewModel,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        EliminarCuentaContent(
            uiState = uiState,
            onVolver = onVolver,
            onContrasenaChange = vm::onContrasenaChange,
            onToggleContrasenaVisible = vm::onToggleContrasenaVisible,
            onEliminarClick = vm::onEliminarClick,
            onConfirmarEliminacion = vm::confirmarEliminacion,
            onDismissDialog = vm::onDismissDialog,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
internal fun EliminarCuentaContent(
    uiState: EliminarCuentaUiState,
    onVolver: () -> Unit,
    onContrasenaChange: (String) -> Unit,
    onToggleContrasenaVisible: () -> Unit,
    onEliminarClick: () -> Unit,
    onConfirmarEliminacion: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            titulo = "Eliminar cuenta",
            onVolver = onVolver,
            iconoVolver = Icons.AutoMirrored.Filled.ArrowBack,
            descripcionVolver = "Volver"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                SettingsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 40.dp)
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = "¿Estás seguro de que deseas eliminar tu cuenta?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Esta acción es permanente y no podrás recuperar tus datos, progreso ni feedback acumulado en ERA.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    EraTextField(
                        value = uiState.contrasena,
                        onValueChange = onContrasenaChange,
                        label = "Confirma tu contraseña",
                        placeholder = "Escribe tu contraseña actual",
                        visualTransformation = if (uiState.contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        iconoFin = if (uiState.contrasenaVisible) EraIcons.VisibilityOff else EraIcons.Visibility,
                        onIconoFinClick = onToggleContrasenaVisible,
                        error = uiState.errorGeneral?.mensajeUsuario()
                    )
                }

                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ColorError,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopCenter)
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            if (uiState.cargando) {
                CircularProgressIndicator(color = ColorPrimary)
            } else {
                EraRegPrimaryButton(
                    texto = "Eliminar mi cuenta",
                    onClick = onEliminarClick,
                    habilitado = uiState.contrasena.isNotBlank(),
                    icono = Icons.Default.Delete
                )
                Spacer(modifier = Modifier.height(16.dp))
                EraRegSecondaryButton(
                    texto = "Cancelar",
                    onClick = onVolver,
                    icono = Icons.Outlined.Close
                )
            }
        }
    }

    if (uiState.mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = {
                Text(
                    text = "Confirmación final",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Confirmas que deseas eliminar tu cuenta de forma definitiva? No hay vuelta atrás."
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmarEliminacion) {
                    Text(
                        text = "SÍ, ELIMINAR",
                        color = ColorError,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(text = "CANCELAR")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Eliminar Cuenta")
@Composable
private fun EliminarCuentaPreview() {
    ERATheme {
        EliminarCuentaContent(
            uiState = EliminarCuentaUiState(),
            onVolver = {},
            onContrasenaChange = {},
            onToggleContrasenaVisible = {},
            onEliminarClick = {},
            onConfirmarEliminacion = {},
            onDismissDialog = {}
        )
    }
}
