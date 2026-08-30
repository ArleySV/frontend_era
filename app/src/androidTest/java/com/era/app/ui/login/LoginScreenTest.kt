package com.era.app.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.EraError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraTituloEInputsVacios() {
        regla.setContent {
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
        regla.onNodeWithText("Inicio de sesión").assertIsDisplayed()
    }

    @Test
    fun muestraErrorCredencialesInvalidas() {
        regla.setContent {
            ERATheme {
                LoginContent(
                    usuarioOCorreo = "u@t.com",
                    contrasena = "pass123",
                    contrasenaVisible = false,
                    cargando = false,
                    errorGeneral = EraError.CredencialesInvalidas,
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
        regla.onNodeWithText("Correo/usuario o contraseña incorrectos").assertIsDisplayed()
    }

    @Test
    fun muestraErrorCuentaBloqueada() {
        regla.setContent {
            ERATheme {
                LoginContent(
                    usuarioOCorreo = "u@t.com",
                    contrasena = "pass123",
                    contrasenaVisible = false,
                    cargando = false,
                    errorGeneral = EraError.CuentaBloqueada,
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
        regla.onNodeWithText("Cuenta bloqueada temporalmente. Intenta de nuevo más tarde.").assertIsDisplayed()
    }

    @Test
    fun muestraErrorConexion() {
        regla.setContent {
            ERATheme {
                LoginContent(
                    usuarioOCorreo = "u@t.com",
                    contrasena = "pass123",
                    contrasenaVisible = false,
                    cargando = false,
                    errorGeneral = EraError.ErrorConexion,
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
        regla.onNodeWithText("Sin conexión. Intenta de nuevo.").assertIsDisplayed()
    }

    @Test
    fun muestraOlvidasteContrasenaYRegistro() {
        regla.setContent {
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
        regla.onNodeWithText("¿Olvidaste la contraseña?").assertIsDisplayed()
    }

    @Test
    fun muestraTextoRegistro() {
        regla.setContent {
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
        regla.onNodeWithText("¿No tienes cuenta? Regístrate").assertIsDisplayed()
    }

    @Test
    fun botonDeshabilitadoConCamposVacios() {
        regla.setContent {
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
        regla.onNodeWithText("Iniciar sesión").assertIsNotEnabled()
    }

    @Test
    fun botonHabilitadoConAmbosCampos() {
        regla.setContent {
            ERATheme {
                LoginContent(
                    usuarioOCorreo = "u@t.com",
                    contrasena = "pass123",
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
        regla.onNodeWithText("Iniciar sesión").assertIsEnabled()
    }

    @Test
    fun toggleVisibilidadConmutaEstado() {
        regla.setContent {
            ERATheme {
                var visibleState by remember { mutableStateOf(false) }
                LoginContent(
                    usuarioOCorreo = "u@t.com",
                    contrasena = "pass123",
                    contrasenaVisible = visibleState,
                    cargando = false,
                    errorGeneral = null,
                    campoConError = null,
                    onUsuarioOCorreoChange = {},
                    onContrasenaChange = {},
                    onContrasenaVisibleToggle = { visibleState = !visibleState },
                    onOlvidasteContrasena = {},
                    onLoginClick = {},
                    onNavegarARegistro = {},
                )
            }
        }
        regla.onNodeWithContentDescription("Mostrar/ocultar contraseña").performClick()
        regla.onNodeWithContentDescription("Mostrar/ocultar contraseña").assertIsDisplayed()
    }

    @Test
    fun registroInvocaOnNavegarARegistro() {
        var registros = 0
        regla.setContent {
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
                    onNavegarARegistro = { registros++ },
                )
            }
        }
        regla.onNode(hasClickAction() and hasAnyAncestor(hasText("¿No tienes cuenta? Regístrate")))
            .performClick()
        regla.runOnIdle { assertEquals(1, registros) }
    }

    @Test
    fun textosDeLosCamposSePropaganAlIntroducir() {
        var usuario = ""
        var contrasena = ""
        regla.setContent {
            ERATheme {
                LoginContent(
                    usuarioOCorreo = usuario,
                    contrasena = contrasena,
                    contrasenaVisible = false,
                    cargando = false,
                    errorGeneral = null,
                    campoConError = null,
                    onUsuarioOCorreoChange = { usuario = it },
                    onContrasenaChange = { contrasena = it },
                    onContrasenaVisibleToggle = {},
                    onOlvidasteContrasena = {},
                    onLoginClick = {},
                    onNavegarARegistro = {},
                )
            }
        }
        regla.onNodeWithTag("campoUsuario")
            .performSemanticsAction(SemanticsActions.SetText) { it(AnnotatedString("usuario@test.com")) }
        regla.onNodeWithTag("campoContrasena")
            .performSemanticsAction(SemanticsActions.SetText) { it(AnnotatedString("clave123")) }
        regla.runOnIdle { assertEquals("usuario@test.com", usuario) }
        regla.runOnIdle { assertEquals("clave123", contrasena) }
    }
}
