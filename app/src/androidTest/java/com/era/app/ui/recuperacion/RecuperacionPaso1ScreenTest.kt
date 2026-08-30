package com.era.app.ui.recuperacion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecuperacionPaso1ScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraCabeceraYSubtitulo() {
        regla.setContent {
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
        regla.onNodeWithText("Recuperar contraseña").assertIsDisplayed()
        regla.onNodeWithText("Te enviamos un código de verificación a tu correo").assertIsDisplayed()
        regla.onNodeWithTag("campoCorreo").assertIsDisplayed()
    }

    @Test
    fun enviarCodigoInvocaCallbackConCorreoVacio() {
        var enviados = 0
        regla.setContent {
            ERATheme {
                RecuperacionPaso1Content(
                    correo = "",
                    onCorreoChange = {},
                    correoError = null,
                    errorGeneral = null,
                    onEnviarCodigo = { enviados++ },
                    onVolverAlLogin = {},
                )
            }
        }
        regla.onNodeWithTag("botonEnviarCodigo").performClick()
        regla.runOnIdle { assertEquals(1, enviados) }
    }

    @Test
    fun volverAlLoginInvocaCallback() {
        var volver = 0
        regla.setContent {
            ERATheme {
                RecuperacionPaso1Content(
                    correo = "",
                    onCorreoChange = {},
                    correoError = null,
                    errorGeneral = null,
                    onEnviarCodigo = {},
                    onVolverAlLogin = { volver++ },
                )
            }
        }
        regla.onNodeWithTag("linkVolverAlLogin").performClick()
        regla.runOnIdle { assertEquals(1, volver) }
    }
}
