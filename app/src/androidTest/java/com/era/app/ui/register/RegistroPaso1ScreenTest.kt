package com.era.app.ui.register

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.era.app.ui.theme.ERATheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.utils.CriteriosContrasena

@RunWith(AndroidJUnit4::class)
class RegistroPaso1ScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraCabeceraYPasoIndicator() {
        regla.setContent {
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
        regla.onNodeWithText("Registro - Paso 1 de 3").assertIsDisplayed()
        regla.onNodeWithText("Datos de usuario").assertIsDisplayed()
    }

    @Test
    fun muestraErroresCuandoCamposEstanVacios() {
        regla.setContent {
            ERATheme {
                RegistroPaso1Content(
                    nombreMenor = "",
                    onNombreMenorChange = {},
                    nombreMenorError = "Ingresa el nombre del menor",
                    fechaNacimiento = "",
                    onFechaNacimientoChange = {},
                    fechaNacimientoError = "Fecha inválida. La edad debe ser entre 7 y 11 años",
                    nombreAcudiente = "",
                    onNombreAcudienteChange = {},
                    nombreAcudienteError = "Ingresa el nombre del acudiente",
                    cedulaAcudiente = "",
                    onCedulaAcudienteChange = {},
                    cedulaAcudienteError = "Solo números, máximo 15 dígitos",
                    onCancelar = {},
                    onContinuar = {},
                )
            }
        }
        regla.onNodeWithText("Ingresa el nombre del menor").assertIsDisplayed()
        regla.onNodeWithText("Ingresa el nombre del acudiente").assertIsDisplayed()
    }
}
