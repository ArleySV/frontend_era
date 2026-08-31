package com.era.app.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class HomePlaceholderScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraSesionIniciadaYCerrarSesion() {
        regla.setContent {
            ERATheme {
                HomePlaceholderScreen(onCerrarSesion = {}, onNavigatePerfil = {}, onNavigateProgreso = {})
            }
        }
        regla.onNodeWithText("Sesión iniciada ✅").assertIsDisplayed()
        regla.onNodeWithText("Cerrar sesión").assertIsDisplayed()
        regla.onNodeWithText("Mi cuenta").assertIsDisplayed()
        regla.onNodeWithText("Mi progreso").assertIsDisplayed()
    }

    @Test
    fun miCuentaInvocaOnNavigatePerfil() {
        var navega = 0
        regla.setContent {
            ERATheme {
                HomePlaceholderScreen(onCerrarSesion = {}, onNavigatePerfil = { navega++ }, onNavigateProgreso = {})
            }
        }
        regla.onNodeWithText("Mi cuenta").performClick()
        regla.runOnIdle { assertEquals(1, navega) }
    }

    @Test
    fun miProgresoInvocaOnNavigateProgreso() {
        var navega = 0
        regla.setContent {
            ERATheme {
                HomePlaceholderScreen(onCerrarSesion = {}, onNavigatePerfil = {}, onNavigateProgreso = { navega++ })
            }
        }
        regla.onNodeWithText("Mi progreso").performClick()
        regla.runOnIdle { assertEquals(1, navega) }
    }

    @Test
    fun cerrarSesionAbreDialogoDeConfirmacion() {
        regla.setContent {
            var visible by remember { mutableStateOf(false) }
            ERATheme {
                HomePlaceholderScreen(
                    onCerrarSesion = { visible = true },
                    onNavigatePerfil = {},
                    onNavigateProgreso = {},
                    dialogoCierreVisible = visible,
                )
            }
        }
        regla.onNodeWithTag("botonCerrarSesion").performClick()
        regla.onNodeWithTag("dialogoCierre").assertIsDisplayed()
        regla.onNodeWithText("¿Deseas cerrar sesión?").assertIsDisplayed()
        regla.onNodeWithText("Sí, cerrar sesión").assertIsDisplayed()
        regla.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun cancelarCierraDialogoSinConfirmar() {
        var cancela = 0
        var confirma = 0
        regla.setContent {
            var visible by remember { mutableStateOf(true) }
            ERATheme {
                HomePlaceholderScreen(
                    onCerrarSesion = {},
                    onNavigatePerfil = {},
                    onNavigateProgreso = {},
                    dialogoCierreVisible = visible,
                    onCancelarCierre = { visible = false; cancela++ },
                    onConfirmarCierre = { confirma++ },
                )
            }
        }
        regla.onNodeWithTag("dialogoCierre").assertIsDisplayed()
        regla.onNodeWithTag("botonCancelarCierre").performClick()
        regla.onNodeWithTag("dialogoCierre").assertDoesNotExist()
        regla.runOnIdle {
            assertEquals(1, cancela)
            assertEquals(0, confirma)
        }
    }

    @Test
    fun confirmarDisparaOnConfirmarCierre() {
        var confirma = 0
        regla.setContent {
            var visible by remember { mutableStateOf(true) }
            ERATheme {
                HomePlaceholderScreen(
                    onCerrarSesion = {},
                    onNavigatePerfil = {},
                    onNavigateProgreso = {},
                    dialogoCierreVisible = visible,
                    onCancelarCierre = { visible = false },
                    onConfirmarCierre = { confirma++ },
                )
            }
        }
        regla.onNodeWithTag("botonConfirmarCierre").performClick()
        regla.runOnIdle { assertEquals(1, confirma) }
    }
}
