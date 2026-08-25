package com.era.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EraRegButtonsTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun botonPrimarioDisparaOnClick() {
        var clics = 0
        regla.setContent {
            ERATheme {
                EraRegPrimaryButton(texto = "Continuar", onClick = { clics++ })
            }
        }
        regla.onNodeWithText("Continuar").performClick()
        regla.runOnIdle { assertEquals(1, clics) }
    }

    @Test
    fun botonPrimarioDeshabilitadoNoDisparaOnClick() {
        var clics = 0
        regla.setContent {
            ERATheme {
                EraRegPrimaryButton(texto = "Continuar", onClick = { clics++ }, habilitado = false)
            }
        }
        regla.onNodeWithText("Continuar").performClick()
        regla.runOnIdle { assertEquals(0, clics) }
    }

    @Test
    fun botonSecundarioDisparaOnClick() {
        var clics = 0
        regla.setContent {
            ERATheme {
                EraRegSecondaryButton(texto = "Cancelar", onClick = { clics++ })
            }
        }
        regla.onNodeWithText("Cancelar").performClick()
        regla.runOnIdle { assertEquals(1, clics) }
    }

    @Test
    fun botonesMuestranSuTextoEIconos() {
        regla.setContent {
            ERATheme {
                EraRegPrimaryButton(texto = "Validar", onClick = {})
                EraRegSecondaryButton(
                    texto = "Atrás",
                    onClick = {},
                    icono = Icons.AutoMirrored.Outlined.ArrowBack,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        regla.onNodeWithText("Validar").assertIsDisplayed()
        regla.onNodeWithText("Atrás").assertIsDisplayed()
    }
}
