package com.era.app.ui.niveles

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.data.model.NivelConProgreso
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NivelesScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private fun emitir(
        niveles: List<NivelConProgreso>,
        onVolver: () -> Unit = {},
        onNivelClick: (Int) -> Unit = {},
    ) {
        regla.setContent {
            ERATheme {
                NivelesScreen(
                    uiState = NivelesUiState(niveles = niveles, cargando = false),
                    onVolver = onVolver,
                    onNivelClick = onNivelClick,
                )
            }
        }
    }

    @Test
    fun cabeceraMuestraTituloYBotonVolver() {
        emitir(niveles = listOf(nivel(1, "DISPONIBLE")))
        regla.onNodeWithTag("titulo_cabecera").assertIsDisplayed()
        regla.onNodeWithText("Trivia primaria").assertIsDisplayed()
        regla.onNodeWithTag("boton_volver").assertIsDisplayed()
    }

    @Test
    fun listaMuestraUnaCardPorNivelConSuOrden() {
        emitir(
            niveles = listOf(
                nivel(1, "COMPLETADO"),
                nivel(2, "DISPONIBLE"),
                nivel(3, "BLOQUEADO"),
            ),
        )
        regla.onNodeWithTag("lista_niveles").assertIsDisplayed()
        regla.onNodeWithTag("nivel_card_1").assertIsDisplayed()
        regla.onNodeWithTag("nivel_card_2").assertIsDisplayed()
        regla.onNodeWithTag("nivel_card_3").assertIsDisplayed()
        regla.onNodeWithText("Nivel 1").assertIsDisplayed()
        regla.onNodeWithText("Nivel 2").assertIsDisplayed()
        regla.onNodeWithText("Nivel 3").assertIsDisplayed()
    }

    @Test
    fun clickEnNivelDisponiblePropagaElOrden() {
        var ordenClick: Int? = null
        emitir(
            niveles = listOf(nivel(1, "DISPONIBLE")),
            onNivelClick = { ordenClick = it },
        )
        regla.onNodeWithTag("nivel_card_1").performClick()
        assertEquals(1, ordenClick)
    }

    @Test
    fun clickEnNivelCompletadoPropagaElOrden() {
        var ordenClick: Int? = null
        emitir(
            niveles = listOf(nivel(1, "COMPLETADO")),
            onNivelClick = { ordenClick = it },
        )
        regla.onNodeWithTag("nivel_card_1").performClick()
        assertEquals(1, ordenClick)
    }

    @Test
    fun nivelBloqueadoNoTieneAccionDeClick() {
        emitir(niveles = listOf(nivel(1, "BLOQUEADO")))
        regla.onNodeWithTag("nivel_card_1").assertHasNoClickAction()
        regla.onNodeWithText("Bloqueado").assertIsDisplayed()
    }

    @Test
    fun botonVolverInvocaOnVolver() {
        var volvio = false
        emitir(
            niveles = listOf(nivel(1, "DISPONIBLE")),
            onVolver = { volvio = true },
        )
        regla.onNodeWithTag("boton_volver").performClick()
        assertTrue(volvio)
    }

    private fun nivel(orden: Int, estado: String) = NivelConProgreso(
        orden = orden,
        pregunta = "P$orden",
        opcionA = "A", opcionB = "B", opcionC = "C",
        respuestaCorrecta = 0,
        estado = estado,
        intentosTotales = 0,
        intentosFallidosConsecutivos = 0,
        completadoEn = null,
        sincronizado = true,
    )
}
