package com.era.app.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.era.app.ui.components.CompactGreenHeader
import com.era.app.ui.components.EraRegPrimaryButton
import com.era.app.ui.components.EraRegSecondaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.StepIndicator
import com.era.app.ui.theme.ColorTextMuted
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.EraError
import com.era.app.utils.mensaje
import com.era.app.utils.mensajeUsuario
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegistroPaso1Screen(
    vm: RegistroViewModel,
    snackbarHostState: SnackbarHostState,
    onCancelar: () -> Unit,
    onNavegarAPaso2: () -> Unit,
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.eventos.collectLatest { evento ->
            when (evento) {
                is RegistroEvento.NavegarAPaso2 -> onNavegarAPaso2()
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

    RegistroPaso1Content(
        nombreMenor = state.nombreMenor,
        onNombreMenorChange = vm::onNombreMenorChange,
        nombreMenorError = CampoRegistro.NOMBRE_MENOR.mensaje(state.errores, state.errorGeneral),
        fechaNacimiento = state.fechaNacimientoDisplay,
        onFechaNacimientoChange = vm::onFechaNacimientoChange,
        fechaNacimientoError = CampoRegistro.FECHA_NACIMIENTO.mensaje(state.errores, state.errorGeneral),
        nombreAcudiente = state.nombreAcudiente,
        onNombreAcudienteChange = vm::onNombreAcudienteChange,
        nombreAcudienteError = CampoRegistro.NOMBRE_ACUDIENTE.mensaje(state.errores, state.errorGeneral),
        cedulaAcudiente = state.cedulaAcudiente,
        onCedulaAcudienteChange = vm::onCedulaAcudienteChange,
        cedulaAcudienteError = CampoRegistro.CEDULA_ACUDIENTE.mensaje(state.errores, state.errorGeneral),
        onCancelar = onCancelar,
        onContinuar = vm::continuarPaso1,
    )
}

@Composable
internal fun RegistroPaso1Content(
    nombreMenor: String,
    onNombreMenorChange: (String) -> Unit,
    nombreMenorError: String?,
    fechaNacimiento: String,
    onFechaNacimientoChange: (String) -> Unit,
    fechaNacimientoError: String?,
    nombreAcudiente: String,
    onNombreAcudienteChange: (String) -> Unit,
    nombreAcudienteError: String?,
    cedulaAcudiente: String,
    onCedulaAcudienteChange: (String) -> Unit,
    cedulaAcudienteError: String?,
    onCancelar: () -> Unit,
    onContinuar: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            CompactGreenHeader(
                titulo = "Registro - Paso 1 de 3",
                subtitulo = "Datos de usuario",
            )
            StepIndicator(pasoActual = 1, modifier = Modifier.padding(vertical = 16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "INFORMACIÓN DEL MENOR",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                EraTextField(
                    value = nombreMenor,
                    onValueChange = onNombreMenorChange,
                    label = "Nombres completos del menor",
                    obligatorio = true,
                    error = nombreMenorError,
                )

                Spacer(modifier = Modifier.height(12.dp))

                EraTextField(
                    value = fechaNacimiento,
                    onValueChange = { onFechaNacimientoChange(formatearFecha(it)) },
                    label = "Fecha de nacimiento",
                    obligatorio = true,
                    placeholder = "DD/MM/AAAA",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    error = fechaNacimientoError,
                    textoAyuda = "Formato: DD/MM/AAAA",
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "DATOS DEL ACUDIENTE",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                EraTextField(
                    value = nombreAcudiente,
                    onValueChange = onNombreAcudienteChange,
                    label = "Nombres del acudiente",
                    obligatorio = true,
                    error = nombreAcudienteError,
                )

                Spacer(modifier = Modifier.height(12.dp))

                EraTextField(
                    value = cedulaAcudiente,
                    onValueChange = onCedulaAcudienteChange,
                    label = "Cédula del acudiente",
                    obligatorio = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    error = cedulaAcudienteError,
                    textoAyuda = "Solo números, máximo 15 dígitos",
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
                texto = "Cancelar",
                onClick = onCancelar,
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

private fun formatearFecha(entrada: String): String {
    val digitos = entrada.filter { it.isDigit() }.take(8)
    return buildString {
        digitos.forEachIndexed { i, c ->
            if (i == 2 || i == 4) append('/')
            append(c)
        }
    }
}

@Preview(showBackground = true, name = "Paso 1 registro")
@Composable
private fun RegistroPaso1Preview() {
    ERATheme {
        RegistroPaso1Content(
            nombreMenor = "",
            onNombreMenorChange = {},
            nombreMenorError = null,
            fechaNacimiento = "",
            onFechaNacimientoChange = {},
            fechaNacimientoError = null,
            nombreAcudiente = "",
            onNombreAcudienteChange = {},
            nombreAcudienteError = null,
            cedulaAcudiente = "",
            onCedulaAcudienteChange = {},
            cedulaAcudienteError = null,
            onCancelar = {},
            onContinuar = {},
        )
    }
}
