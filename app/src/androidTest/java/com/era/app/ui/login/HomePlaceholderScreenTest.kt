package com.era.app.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
                HomePlaceholderScreen(onCerrarSesion = {}, onNavigatePerfil = {})
            }
        }
        regla.onNodeWithText("Sesión iniciada ✅").assertIsDisplayed()
        regla.onNodeWithText("Cerrar sesión").assertIsDisplayed()
        regla.onNodeWithText("Mi cuenta").assertIsDisplayed()
    }

    @Test
    fun miCuentaInvocaOnNavigatePerfil() {
        var navega = 0
        regla.setContent {
            ERATheme {
                HomePlaceholderScreen(onCerrarSesion = {}, onNavigatePerfil = { navega++ })
            }
        }
        regla.onNodeWithText("Mi cuenta").performClick()
        regla.runOnIdle { assertEquals(1, navega) }
    }

    @Test
    fun cerrarSesionInvocaOnCerrarSesion() {
        var cierra = 0
        regla.setContent {
            ERATheme {
                HomePlaceholderScreen(onCerrarSesion = { cierra++ }, onNavigatePerfil = {})
            }
        }
        regla.onNodeWithText("Cerrar sesión").performClick()
        regla.runOnIdle { assertEquals(1, cierra) }
    }
}
