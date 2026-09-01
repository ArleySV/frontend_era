package com.era.app.ui.perfil

import android.content.Context
import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.era.app.R
import com.era.app.remote.dto.user.UserProfile
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.SettingsCard
import com.era.app.ui.components.SettingsCardRow
import com.era.app.ui.components.SettingsHeader
import com.era.app.ui.components.avatar.AvatarSelector
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorPrimaryLight
import com.era.app.ui.theme.ColorTextWhite
import com.era.app.ui.theme.ERATheme
import com.era.app.repository.Resultado
import com.era.app.utils.ArchivoAvatar
import com.era.app.utils.AvatarFileValidator
import com.era.app.utils.Validators
import com.era.app.utils.mensajeUsuario

@Composable
fun MiCuentaScreen(
    onVolver: () -> Unit,
    onNavegarALogin: () -> Unit,
    onNavegarAEliminarCuenta: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiCuentaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventos = viewModel.eventos
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val resultado = leerImagenDesdeUri(context, uri)
            if (resultado != null) {
                viewModel.onAvatarSeleccionado(resultado)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEntrar()
        eventos.collect { evento ->
            when (evento) {
                is MiCuentaEvento.NavegarALogin -> onNavegarALogin()
                is MiCuentaEvento.MostrarSnackbar -> snackbarHostState.showSnackbar(evento.mensaje)
            }
        }
    }

    LaunchedEffect(uiState.errorAvatar) {
        uiState.errorAvatar?.let { error ->
            snackbarHostState.showSnackbar(error.mensajeUsuario())
            viewModel.onLimpiarErrorAvatar()
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
            onEliminarCuenta = onNavegarAEliminarCuenta,
            onCambiarAvatar = viewModel::onCambiarAvatarClick,
            onCerrarSelector = viewModel::onCerrarSelector,
            onSeleccionarPreset = viewModel::onSeleccionarPreset,
            onSubirFoto = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
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
    onEliminarCuenta: () -> Unit,
    onCambiarAvatar: () -> Unit = {},
    onCerrarSelector: () -> Unit = {},
    onSeleccionarPreset: (Int) -> Unit = {},
    onSubirFoto: () -> Unit = {},
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
                                    mostrarDivisor = true,
                                )
                                SettingsCardRow(
                                    label = "Seguridad",
                                    value = "Eliminar mi cuenta",
                                    icono = Icons.Filled.Delete,
                                    descripcionIcono = "Icono de eliminar",
                                    mostrarDivisor = false,
                                    accion = {
                                        TextButton(onClick = onEliminarCuenta) {
                                            Text(
                                                text = "Gestionar",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = ColorError,
                                            )
                                        }
                                    },
                                )
                            }
                            AvatarPerfil(
                                avatar = perfil.avatar,
                                bytesAvatarPersonalizado = uiState.bytesAvatarPersonalizado,
                                subiendoAvatar = uiState.subiendoAvatar,
                                nombreMenor = perfil.nombreMenor,
                                onClick = onCambiarAvatar,
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

    if (uiState.selectorAvatarAbierto) {
        AlertDialog(
            onDismissRequest = onCerrarSelector,
            title = {
                Text(
                    text = "Elegir un buen avatar",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                AvatarSelector(
                    seleccionado = uiState.avatarPresetSeleccionado,
                    onSeleccionar = onSeleccionarPreset,
                    mostrarMas = true,
                    onMas = onSubirFoto,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = onCerrarSelector) {
                    Text(
                        text = "Cerrar",
                        color = ColorPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}

@Composable
private fun AvatarPerfil(
    avatar: String?,
    bytesAvatarPersonalizado: ByteArray?,
    subiendoAvatar: Boolean,
    nombreMenor: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val iniciales = nombreMenor
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifEmpty { "?" }

    var cargandoImagen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(100.dp)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .background(ColorPrimaryLight)
            .border(width = 4.dp, color = ColorTextWhite, shape = CircleShape)
            .clickable(onClick = onClick)
            .testTag("avatarTrigger"),
        contentAlignment = Alignment.Center,
    ) {
        val preset = avatar?.let { extraerPreset(it) }
        val esCustom = avatar?.startsWith("custom:") == true

        when {
            esCustom && bytesAvatarPersonalizado != null -> {
                AsyncImage(
                    model = bytesAvatarPersonalizado,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        cargandoImagen = state is AsyncImagePainter.State.Loading
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            preset != null -> {
                Image(
                    painter = painterResource(preset),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                Text(
                    text = iniciales,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary,
                )
            }
        }

        if (subiendoAvatar || (esCustom && bytesAvatarPersonalizado != null && cargandoImagen)) {
            CircularProgressIndicator(
                color = ColorPrimary,
                modifier = Modifier.size(28.dp),
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

/**
 * Lee de la URI de la imagen seleccionada y delega la validación al helper puro (D-57).
 * Se ejecuta en la capa UI (con ContentResolver de LocalContext). Si el tamaño del
 * archivo (vía AssetFileDescriptor.length) supera el límite NO se lee el binario en
 * memoria y se usa la sobrecarga `validar(size, mime)`; si está dentro del límite se
 * leen los bytes y se valida con `validar(bytes, ...)`. Cero logs de bytes/filename/URI.
 * Devuelve el Resultado ya validado, o null si no se pudo leer.
 */
private fun leerImagenDesdeUri(context: Context, uri: Uri): Resultado<ArchivoAvatar>? {
    return try {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val filename = consultarNombreArchivo(resolver, uri)

        val fd: AssetFileDescriptor? = resolver.openAssetFileDescriptor(uri, "r")
        val longitud = fd?.length
        fd?.close()

        if (longitud == null || longitud <= AvatarFileValidator.MAX_BYTES_AVATAR) {
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            AvatarFileValidator.validar(bytes, filename, mimeType)
        } else {
            // Archivo demasiado grande: no leemos el binario (D-57) y validamos solo por
            // tamaño/MIME. La sobrecarga SIN bytes evita fabricar un ByteArray sintético.
            AvatarFileValidator.validar(longitud, mimeType)
        }
    } catch (e: Exception) {
        null
    }
}

private fun consultarNombreArchivo(resolver: ContentResolver, uri: Uri): String? {
    return try {
        val cursor = resolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else null
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
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
            onEliminarCuenta = {},
        )
    }
}
