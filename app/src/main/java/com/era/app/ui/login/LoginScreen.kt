package com.era.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.era.app.ui.components.EraIcons
import com.era.app.ui.components.HeroLogin
import com.era.app.ui.components.LoginButton
import com.era.app.ui.components.LoginInputPill
import com.era.app.ui.theme.ColorError
import com.era.app.ui.theme.ColorPrimary
import com.era.app.ui.theme.ColorSurface
import com.era.app.ui.theme.ColorTextBody
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.EraError

@Composable
fun LoginContent(
    usuarioOCorreo: String,
    contrasena: String,
    contrasenaVisible: Boolean,
    cargando: Boolean,
    errorGeneral: EraError?,
    campoConError: CampoLogin?,
    onUsuarioOCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onContrasenaVisibleToggle: () -> Unit,
    onOlvidasteContrasena: () -> Unit,
    onLoginClick: () -> Unit,
    onNavegarARegistro: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorSurface),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeroLogin(modifier = Modifier.fillMaxWidth())

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(ColorSurface),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                Text(
                    text = "Inicio de sesión",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, bottom = 28.dp),
                )

                errorGeneral?.let { error ->
                    val mensaje = when (error) {
                        is EraError.CredencialesInvalidas ->
                            "Correo/usuario o contraseña incorrectos"
                        is EraError.CuentaBloqueada ->
                            "Cuenta bloqueada temporalmente. Intenta de nuevo más tarde."
                        is EraError.CuentaInactiva ->
                            "Tu cuenta fue desactivada."
                        is EraError.ErrorConexion ->
                            "Sin conexión. Intenta de nuevo."
                        is EraError.ErrorServidor ->
                            "Intenta más tarde."
                        else -> "Ocurrió un error inesperado."
                    }
                    Text(
                        text = mensaje,
                        color = ColorError,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                LoginInputPill(
                    value = usuarioOCorreo,
                    onValueChange = onUsuarioOCorreoChange,
                    placeholder = "ID/E-mail",
                    leadingIcon = EraIcons.EmailOutline,
                    isError = campoConError == CampoLogin.USUARIO_O_CORREO,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .height(58.dp)
                        .testTag("campoUsuario"),
                )

                Spacer(modifier = Modifier.height(20.dp))

                LoginInputPill(
                    value = contrasena,
                    onValueChange = onContrasenaChange,
                    placeholder = "Contraseña",
                    leadingIcon = EraIcons.LockOutline,
                    trailingIcon = if (contrasenaVisible) {
                        EraIcons.VisibilityOff
                    } else {
                        EraIcons.Visibility
                    },
                    onTrailingIconClick = onContrasenaVisibleToggle,
                    isError = campoConError == CampoLogin.CONTRASENA,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (contrasenaVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .height(58.dp)
                        .testTag("campoContrasena"),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        withLink(LinkAnnotation.Clickable(tag = "olvidaste") { onOlvidasteContrasena() }) {
                            withStyle(style = SpanStyle(color = ColorPrimary, fontWeight = FontWeight.Medium)) {
                                append("¿Olvidaste la contraseña?")
                            }
                        }
                    },
                    style = LocalTextStyle.current.copy(fontSize = 16.sp, textAlign = TextAlign.Center),
                    modifier = Modifier.padding(vertical = 8.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                LoginButton(
                    text = "Iniciar sesión",
                    onClick = onLoginClick,
                    enabled = usuarioOCorreo.isNotBlank() && contrasena.isNotBlank(),
                    cargando = cargando,
                    modifier = Modifier.widthIn(max = 300.dp),
                )

                Spacer(modifier = Modifier.weight(1f))

                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = ColorTextBody, fontWeight = FontWeight.Normal)) {
                        append("¿No tienes cuenta? ")
                    }
                    withLink(LinkAnnotation.Clickable(tag = "registro") { onNavegarARegistro() }) {
                        withStyle(style = SpanStyle(color = ColorPrimary, fontWeight = FontWeight.Bold)) {
                            append("Regístrate")
                        }
                    }
                }
                Text(
                    text = annotatedText,
                    style = LocalTextStyle.current.copy(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 44.dp),
                )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    onNavigateARecuperacion: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    backStackEntry: androidx.navigation.NavBackStackEntry? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventos = viewModel.eventos

    LaunchedEffect(Unit) {
        if (backStackEntry?.savedStateHandle?.remove<Boolean>("registro_exitoso") == true) {
            snackbarHostState.showSnackbar("Cuenta verificada")
        }
        if (backStackEntry?.savedStateHandle?.remove<Boolean>("recuperacion_exitosa") == true) {
            snackbarHostState.showSnackbar("Contraseña actualizada. Ya puedes iniciar sesión.")
        }
        eventos.collect { evento ->
            when (evento) {
                is LoginEvento.NavegarAHome -> onNavigateToHome()
                is LoginEvento.NavegarALogin -> { /* ya estamos en login */ }
                is LoginEvento.NavegarARegistro -> onNavigateToRegistro()
                is LoginEvento.NavegarARecuperacion -> onNavigateARecuperacion()
                is LoginEvento.MostrarSnackbar -> {
                    snackbarHostState.showSnackbar(evento.mensaje)
                }
            }
        }
    }

    LoginContent(
        usuarioOCorreo = uiState.usuarioOCorreo,
        contrasena = uiState.contrasena,
        contrasenaVisible = uiState.contrasenaVisible,
        cargando = uiState.cargando,
        errorGeneral = uiState.errorGeneral,
        campoConError = uiState.campoConError,
        onUsuarioOCorreoChange = viewModel::onUsuarioOCorreoChange,
        onContrasenaChange = viewModel::onContrasenaChange,
        onContrasenaVisibleToggle = viewModel::onContrasenaVisibleToggle,
        onOlvidasteContrasena = viewModel::onOlvidasteContrasena,
        onLoginClick = viewModel::onLoginClick,
        onNavegarARegistro = viewModel::onNavegarARegistro,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Preview(showBackground = true, name = "Login Screen")
@Composable
private fun LoginScreenPreview() {
    ERATheme {
        LoginContent(
            usuarioOCorreo = "",
            contrasena = "",
            contrasenaVisible = false,
            cargando = false,
            errorGeneral = null,
            campoConError = null,
            onUsuarioOCorreoChange = {},
            onContrasenaChange = {},
            onContrasenaVisibleToggle = {},
            onOlvidasteContrasena = {},
            onLoginClick = {},
            onNavegarARegistro = {},
        )
    }
}
