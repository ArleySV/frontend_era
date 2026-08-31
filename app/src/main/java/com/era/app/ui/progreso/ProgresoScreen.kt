package com.era.app.ui.progreso

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.era.app.data.model.NivelConProgreso
import com.era.app.ui.components.EraRegSecondaryButton
import com.era.app.ui.components.EraTextField
import com.era.app.ui.components.SettingsCard
import com.era.app.ui.components.SettingsCardRow
import com.era.app.ui.components.SettingsHeader
import com.era.app.ui.theme.*
import com.era.app.utils.mensajeUsuario

@Composable
fun ProgresoScreen(
    vm: ProgresoViewModel,
    onVolver: () -> Unit,
    onSesionExpirada: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.eventos.collect { evento ->
            when (evento) {
                is ProgresoEvento.Error -> snackbarHostState.showSnackbar(evento.error.mensajeUsuario())
                is ProgresoEvento.SesionExpirada -> onSesionExpirada()
                is ProgresoEvento.ResetExitoso -> snackbarHostState.showSnackbar("Progreso reiniciado con éxito")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        ProgresoContent(
            uiState = uiState,
            onVolver = onVolver,
            onSincronizar = vm::sincronizar,
            onReiniciarProgreso = vm::onReiniciarProgresoClick,
            onContrasenaResetChange = vm::onContrasenaResetChange,
            onConfirmarReset = vm::onConfirmarReset,
            onCancelarReset = vm::onCancelarReset,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
internal fun ProgresoContent(
    uiState: ProgresoUiState,
    onVolver: () -> Unit,
    onSincronizar: () -> Unit,
    onReiniciarProgreso: () -> Unit,
    onContrasenaResetChange: (String) -> Unit,
    onConfirmarReset: () -> Unit,
    onCancelarReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SettingsHeader(
            titulo = "Progreso",
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
            // Tarjeta de Porcentaje
            Box(modifier = Modifier.fillMaxWidth()) {
                SettingsCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 40.dp)
                ) {
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "Tu avance actual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorSettingsLabel,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    val porcentajeAnimado by animateFloatAsState(
                        targetValue = uiState.porcentaje,
                        label = "porcentaje"
                    )
                    
                    Text(
                        text = "${(uiState.porcentaje * 100).toInt()}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { porcentajeAnimado },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = ColorPrimary,
                        trackColor = Color(0xFFD5D5D5)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "${uiState.nivelesCompletados} de 20 niveles completados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTextMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ColorTextWhite)
                        .shadow(4.dp, CircleShape)
                        .background(ColorPrimaryPale, CircleShape)
                        .align(Alignment.TopCenter)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Tarjeta de Estadísticas
            SettingsCard(modifier = Modifier.fillMaxWidth()) {
                SettingsCardRow(
                    label = "Total de intentos",
                    value = uiState.reintentosTotales.toString(),
                    icono = Icons.Default.Refresh,
                    descripcionIcono = "Intentos",
                    mostrarDivisor = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de Acción
            if (uiState.sincronizando) {
                CircularProgressIndicator(color = ColorPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sincronizando...", style = MaterialTheme.typography.labelMedium)
            } else {
                Button(
                    onClick = onSincronizar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sincronizar ahora")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            EraRegSecondaryButton(
                texto = "Reiniciar mi progreso",
                onClick = onReiniciarProgreso,
                icono = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.dialogoResetVisible) {
        AlertDialog(
            onDismissRequest = onCancelarReset,
            title = { Text("Reiniciar progreso", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Esta acción borrará todo tu avance y volverás al nivel 1. No se puede deshacer.")
                    Spacer(modifier = Modifier.height(16.dp))
                    EraTextField(
                        value = uiState.contrasenaReset,
                        onValueChange = onContrasenaResetChange,
                        label = "Confirma con tu contraseña",
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        error = uiState.error?.mensajeUsuario()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmarReset,
                    enabled = !uiState.reseteando && uiState.contrasenaReset.isNotBlank()
                ) {
                    if (uiState.reseteando) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("SÍ, REINICIAR", color = ColorError, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarReset, enabled = !uiState.reseteando) {
                    Text("CANCELAR")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgresoPreview() {
    ERATheme {
        ProgresoContent(
            uiState = ProgresoUiState(
                nivelesCompletados = 5,
                porcentaje = 0.25f,
                reintentosTotales = 12
            ),
            onVolver = {},
            onSincronizar = {},
            onReiniciarProgreso = {},
            onContrasenaResetChange = {},
            onConfirmarReset = {},
            onCancelarReset = {}
        )
    }
}
