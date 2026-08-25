package com.era.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EraTextFieldTest {

    @get:Rule
    val regla = createComposeRule()

    private var valorCapturado by mutableStateOf("")

    @Test
    fun muestraAsteriscoSoloSiEsObligatorio() {
        regla.setContent {
            ERATheme {
                EraTextField(value = "", onValueChange = {}, label = "Correo electrónico", obligatorio = true)
                EraTextField(value = "", onValueChange = {}, label = "Nombre de usuario")
            }
        }
        regla.onNodeWithText("Correo electrónico").assertExists()
        regla.onNodeWithText(" *").assertExists()
        regla.onNodeWithText("Nombre de usuario").assertExists()
    }

    @Test
    fun muestraElErrorConPrioridadSobreLaAyuda() {
        regla.setContent {
            ERATheme {
                EraTextField(
                    value = "",
                    onValueChange = {},
                    label = "Código",
                    error = "Ingresa 6 dígitos numéricos",
                    textoAyuda = "Revisa tu correo"
                )
            }
        }
        regla.onNodeWithText("Ingresa 6 dígitos numéricos").assertIsDisplayed()
        regla.onNodeWithText("Revisa tu correo").assertDoesNotExist()
    }

    @Test
    fun muestraTextoDeAyudaCuandoNoHayError() {
        regla.setContent {
            ERATheme {
                EraTextField(
                    value = "",
                    onValueChange = {},
                    label = "Contraseña",
                    textoAyuda = "Mín. 8 caracteres"
                )
            }
        }
        regla.onNodeWithText("Mín. 8 caracteres").assertIsDisplayed()
    }

    @Test
    fun muestraPlaceholderCuandoEstaVacio() {
        regla.setContent {
            ERATheme {
                EraTextField(
                    value = "",
                    onValueChange = {},
                    label = "Correo",
                    placeholder = "correo@ejemplo.com"
                )
            }
        }
        regla.onNodeWithText("correo@ejemplo.com").assertIsDisplayed()
    }

    @Test
    fun propagaCambiosDeTexto() {
        regla.setContent {
            ERATheme {
                EraTextField(
                    value = valorCapturado,
                    onValueChange = { valorCapturado = it },
                    label = "Usuario"
                )
            }
        }
        regla.onNode(hasSetTextAction()).performTextInput("abc")
        regla.runOnIdle { assertEquals("abc", valorCapturado) }
    }

    @Test
    fun ocultaElPlaceholderAlEscribir() {
        regla.setContent {
            ERATheme {
                EraTextField(
                    value = valorCapturado,
                    onValueChange = { valorCapturado = it },
                    label = "Correo",
                    iconoInicio = Icons.Outlined.Email,
                    placeholder = "correo@ejemplo.com"
                )
            }
        }
        regla.onNodeWithText("correo@ejemplo.com").assertIsDisplayed()
        regla.onNode(hasSetTextAction()).performTextInput("x")
        regla.onNodeWithText("correo@ejemplo.com").assertDoesNotExist()
    }
}
