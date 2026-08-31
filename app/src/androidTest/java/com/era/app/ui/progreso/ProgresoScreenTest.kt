package com.era.app.ui.progreso

import androidx.compose.runtime.Composable
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
class ProgresoScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Composable
    private fun emitir(uiState: ProgresoUiState = ProgresoUiState()) =
        ProgresoContent(
            uiState = uiState,
            onVolver = {},
            onSincronizar = {},
            onReiniciarProgreso = {},
            onContrasenaResetChange = {},
            onConfirmarReset = {},
            onCancelarReset = {}
        )

    @Test
    fun cargaMuestraTituloYEstadisticas() {
        regla.setContent { 
            ERATheme { 
                emitir(ProgresoUiState(nivelesCompletados = 5, porcentaje = 0.25f, reintentosTotales = 10)) 
            } 
        }
        regla.onNodeWithText("Progreso").assertIsDisplayed()
        regla.onNodeWithText("25%").assertIsDisplayed()
        regla.onNodeWithText("5 de 20 niveles completados").assertIsDisplayed()
        regla.onNodeWithText("Total de intentos").assertIsDisplayed()
        regla.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun botonSincronizarInvocaCallback() {
        var syncLlamado = 0
        regla.setContent {
            ERATheme {
                ProgresoContent(
                    uiState = ProgresoUiState(),
                    onVolver = {},
                    onSincronizar = { syncLlamado++ },
                    onReiniciarProgreso = {},
                    onContrasenaResetChange = {},
                    onConfirmarReset = {},
                    onCancelarReset = {}
                )
            }
        }
        regla.onNodeWithText("Sincronizar ahora").performClick()
        assertEquals(1, syncLlamado)
    }

    @Test
    fun dialogoResetSeMuestra() {
        regla.setContent {
            ERATheme {
                emitir(ProgresoUiState(dialogoResetVisible = true))
            }
        }
        regla.onNodeWithText("Reiniciar progreso").assertIsDisplayed()
        regla.onNodeWithText("Esta acción borrará todo tu avance y volverás al nivel 1. No se puede deshacer.").assertIsDisplayed()
        regla.onNodeWithText("CANCELAR").assertIsDisplayed()
    }
}
