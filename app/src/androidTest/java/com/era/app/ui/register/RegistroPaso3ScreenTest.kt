package com.era.app.ui.register

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.era.app.ui.theme.ERATheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class RegistroPaso3ScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraCountdownCuandoHaySegundosRestantes() {
        regla.setContent {
            ERATheme {
                RegistroPaso3Content(
                    correo = "test@ejemplo.com",
                    codigoOtp = "",
                    onCodigoOtpChange = {},
                    codigoOtpError = null,
                    countdownHabilitado = false,
                    countdownTexto = "Reenviar código (32s)",
                    onReenviarCodigo = {},
                    onVerificarCodigo = {},
                )
            }
        }
        regla.onNodeWithText("test@ejemplo.com").assertIsDisplayed()
        regla.onNodeWithText("Reenviar código (32s)").assertIsDisplayed()
    }

    @Test
    fun muestraReenviarHabilitadoCuandoCountdownEsCero() {
        regla.setContent {
            ERATheme {
                RegistroPaso3Content(
                    correo = "test@ejemplo.com",
                    codigoOtp = "",
                    onCodigoOtpChange = {},
                    codigoOtpError = null,
                    countdownHabilitado = true,
                    countdownTexto = "Reenviar código",
                    onReenviarCodigo = {},
                    onVerificarCodigo = {},
                )
            }
        }
        regla.onNodeWithText("Reenviar código").assertIsDisplayed()
    }
}
