package com.era.app.ui.recuperacion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecuperacionPaso2ScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraCodigoEnviadoACorreo() {
        regla.setContent {
            ERATheme {
                RecuperacionPaso2Content(
                    correo = "correo@ejemplo.com",
                    codigoOtp = "",
                    onCodigoOtpChange = {},
                    codigoOtpError = null,
                    errorGeneral = null,
                    countdownHabilitado = true,
                    countdownTexto = "Reenviar código",
                    onReenviarCodigo = {},
                    onAtras = {},
                    onVerificarCodigo = {},
                )
            }
        }
        regla.onNodeWithText("Código enviado a").assertIsDisplayed()
        regla.onNodeWithText("correo@ejemplo.com").assertIsDisplayed()
    }

    @Test
    fun codigoOtpSePropagaAlIntroducir() {
        var codigo = ""
        regla.setContent {
            ERATheme {
                RecuperacionPaso2Content(
                    correo = "correo@ejemplo.com",
                    codigoOtp = codigo,
                    onCodigoOtpChange = { codigo = it },
                    codigoOtpError = null,
                    errorGeneral = null,
                    countdownHabilitado = true,
                    countdownTexto = "Reenviar código",
                    onReenviarCodigo = {},
                    onAtras = {},
                    onVerificarCodigo = {},
                )
            }
        }
        regla.onNode(hasSetTextAction()).performTextInput("123456")
        regla.runOnIdle { assertEquals("123456", codigo) }
    }

    @Test
    fun verificarCodigoInvocaCallback() {
        var verifica = 0
        regla.setContent {
            ERATheme {
                RecuperacionPaso2Content(
                    correo = "correo@ejemplo.com",
                    codigoOtp = "123456",
                    onCodigoOtpChange = {},
                    codigoOtpError = null,
                    errorGeneral = null,
                    countdownHabilitado = true,
                    countdownTexto = "Reenviar código",
                    onReenviarCodigo = {},
                    onAtras = {},
                    onVerificarCodigo = { verifica++ },
                )
            }
        }
        regla.onNodeWithTag("botonVerificarCodigo").performClick()
        regla.runOnIdle { assertEquals(1, verifica) }
    }

    @Test
    fun reenviarDeshabilitadoConCountdownActivo() {
        regla.setContent {
            ERATheme {
                RecuperacionPaso2Content(
                    correo = "correo@ejemplo.com",
                    codigoOtp = "",
                    onCodigoOtpChange = {},
                    codigoOtpError = null,
                    errorGeneral = null,
                    countdownHabilitado = false,
                    countdownTexto = "Reenviar código (32s)",
                    onReenviarCodigo = {},
                    onAtras = {},
                    onVerificarCodigo = {},
                )
            }
        }
        regla.onNodeWithText("Reenviar código (32s)").assertIsDisplayed()
        regla.onNodeWithText("Reenviar código (32s)").assertIsNotEnabled()
    }

    @Test
    fun reenviarHabilitadoSinCountdown() {
        var reenvios = 0
        regla.setContent {
            ERATheme {
                RecuperacionPaso2Content(
                    correo = "correo@ejemplo.com",
                    codigoOtp = "",
                    onCodigoOtpChange = {},
                    codigoOtpError = null,
                    errorGeneral = null,
                    countdownHabilitado = true,
                    countdownTexto = "Reenviar código",
                    onReenviarCodigo = { reenvios++ },
                    onAtras = {},
                    onVerificarCodigo = {},
                )
            }
        }
        regla.onNodeWithText("Reenviar código").assertIsDisplayed()
        regla.onNodeWithText("Reenviar código").assertIsEnabled().performClick()
        regla.runOnIdle { assertEquals(1, reenvios) }
    }
}
