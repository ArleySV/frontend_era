package com.era.app.ui.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StepIndicatorTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraTresPuntos() {
        regla.setContent {
            ERATheme {
                StepIndicator(pasoActual = 1)
            }
        }
        regla.onAllNodesWithTag("step_indicator_punto_0", useUnmergedTree = true)
            .assertCountEquals(1)
        regla.onAllNodesWithTag("step_indicator_punto_2", useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun marcaUnicamenteElPasoActivo() {
        regla.setContent {
            ERATheme {
                StepIndicator(pasoActual = 2)
            }
        }
        regla.onNodeWithContentDescription("Paso 2 de 3 activo").assertExists()
        regla.onNodeWithContentDescription("Paso 1 de 3 activo").assertDoesNotExist()
        regla.onNodeWithContentDescription("Paso 3 de 3 activo").assertDoesNotExist()
    }

    @Test
    fun respetaElTotalDePasosPersonalizado() {
        regla.setContent {
            ERATheme {
                StepIndicator(pasoActual = 3, totalPasos = 5)
            }
        }
        regla.onNodeWithContentDescription("Paso 3 de 5 activo").assertExists()
        regla.onAllNodesWithTag("step_indicator_punto_4", useUnmergedTree = true)
            .assertCountEquals(1)
    }
}
