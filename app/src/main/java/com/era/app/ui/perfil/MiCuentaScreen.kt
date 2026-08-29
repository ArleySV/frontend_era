package com.era.app.ui.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.era.app.R
import com.era.app.remote.dto.user.UserProfile
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.SettingsCard
import com.era.app.ui.components.SettingsCardRow
import com.era.app.ui.components.SettingsHeader
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorPrimaryLight
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.Validators
import com.era.app.utils.mensajeUsuario

@Composable
fun MiCuentaScreen(
    onVolver: () -> Unit,
    onNavegarALogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiCuentaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventos = viewModel.eventos
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEntrar()
        eventos.collect { evento ->
            when (evento) {
                is MiCuentaEvento.NavegarALogin -> onNavegarALogin()
                is MiCuentaEvento.MostrarSnackbar -> snackbarHostState.showSnackbar(evento.mensaje)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MiCuentaContent(
            uiState = uiState,
            onVolver = onVolver,
            onEditar = viewModel::onEditarClick,
            onDialogCancelar = viewModel::onDialogCancelar,
            onNombreUsuarioChange = viewModel::onNombreUsuarioChange,
            onGuardar = viewModel::onGuardarClick,
            onReintentar = viewModel::onReintentar,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
internal fun MiCuentaContent(
    uiState: MiCuentaUiState,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onDialogCancelar: () -> Unit,
    onNombreUsuarioChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            titulo = "Mi Cuenta",
            onVolver = onVolver,
            iconoVolver = Icons.AutoMirrored.Filled.ArrowBack,
            descripcionVolver = "Volver",
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                uiState.cargando -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    SettingsCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                uiState.errorGeneral != null -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    SettingsCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.errorGeneral.mensajeUsuario(),
                                color = ColorError,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onReintentar,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) {
                                Text(text = "Reintentar")
                            }
                        }
                    }
                }
                uiState.perfil != null -> {
                    val perfil = uiState.perfil
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            SettingsCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = 50.dp),
                            ) {
                                Spacer(modifier = Modifier.height(48.dp))
                                SettingsCardRow(
                                    label = "Nombre del menor",
                                    value = perfil.nombreMenor,
                                    icono = Icons.Filled.Person,
                                    descripcionIcono = "Icono del menor",
                                )
                                SettingsCardRow(
                                    label = "Correo electrónico",
                                    value = perfil.correo,
                                    icono = Icons.Filled.Email,
                                    descripcionIcono = "Icono de correo",
                                )
                                SettingsCardRow(
                                    label = "Nombre de usuario",
                                    value = perfil.nombreUsuario,
                                    icono = Icons.Filled.Person,
                                    descripcionIcono = "Icono de usuario",
                                    accion = {
                                        TextButton(onClick = onEditar) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = null,
                                                    tint = ColorPrimary,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Editar",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = ColorPrimary,
                                                )
                                            }
                                        }
                                    },
                                )
                                SettingsCardRow(
                                    label = "Fecha de nacimiento",
                                    value = Validators.formatearFechaISO(perfil.fechaNacimiento)
                                        ?: perfil.fechaNacimiento,
                                    icono = Icons.Filled.DateRange,
                                    descripcionIcono = "Icono de fecha",
                                    mostrarDivisor = false,
                                )
                            }
                            AvatarPerfil(
                                avatar = perfil.avatar,
                                nombreMenor = perfil.nombreMenor,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .zIndex(1f),
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (uiState.dialogoAbierto) {
        DialogEditarNombreUsuario(
            nombreUsuario = uiState.nombreUsuario,
            error = uiState.errorNombreUsuario,
            onNombreUsuarioChange = onNombreUsuarioChange,
            onCancelar = onDialogCancelar,
            onGuardar = onGuardar,
        )
    }
}

@Composable
private fun AvatarPerfil(
    avatar: String?,
    nombreMenor: String,
    modifier: Modifier = Modifier,
) {
    val iniciales = nombreMenor
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .size(100.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .background(ColorPrimaryLight)
            .border(width = 4.dp, color = ColorTextWhite, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val preset = avatar?.let { extraerPreset(it) }
        if (preset != null) {
            Image(
                painter = painterResource(preset),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = iniciales,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPrimary,
            )
        }
    }
}

private fun extraerPreset(avatar: String): Int? = when (avatar) {
    "preset:1" -> R.drawable.avatar_preset_1
    "preset:2" -> R.drawable.avatar_preset_2
    "preset:3" -> R.drawable.avatar_preset_3
    else -> null
}

@Composable
private fun DialogEditarNombreUsuario(
    nombreUsuario: String,
    error: String?,
    onNombreUsuarioChange: (String) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(text = "Editar nombre de usuario") },
        text = {
            EraTextField(
                value = nombreUsuario,
                onValueChange = onNombreUsuarioChange,
                label = "Nombre de usuario",
                placeholder = "3-60 caracteres, sin espacios",
                error = error,
            )
        },
        confirmButton = {
            TextButton(onClick = onGuardar) {
                Text(text = "Guardar", color = ColorPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = "Cancelar")
            }
        },
    )
}

@Preview(showBackground = true, name = "Mi Cuenta")
@Composable
private fun MiCuentaPreview() {
    ERATheme {
        MiCuentaContent(
            uiState = MiCuentaUiState(
                perfil = UserProfile(
                    nombreMenor = "María López",
                    fechaNacimiento = "2015-03-15",
                    correo = "maria@ejemplo.com",
                    nombreUsuario = "@maria_lopez",
                    avatar = null,
                ),
            ),
            onVolver = {},
            onEditar = {},
            onDialogCancelar = {},
            onNombreUsuarioChange = {},
            onGuardar = {},
            onReintentar = {},
        )
    }
}
