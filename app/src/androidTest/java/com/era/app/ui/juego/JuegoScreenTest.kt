package com.era.app.ui.juego

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.data.model.NivelConProgreso
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JuegoScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private fun emitir(
        uiState: JuegoUiState,
        onOpcionClick: (Int) -> Unit = {},
        onAbrirMenu: () -> Unit = {},
        onContinuar: () -> Unit = {},
        onReiniciar: () -> Unit = {},
        onSalir: () -> Unit = {},
    ) {
        regla.setContent {
            ERATheme {
                JuegoScreen(
                    uiState = uiState,
                    onOpcionClick = onOpcionClick,
                    onAbrirMenu = onAbrirMenu,
                    onContinuar = onContinuar,
                    onReiniciar = onReiniciar,
                    onSalir = onSalir,
                )
            }
        }
    }

    @Test
    fun cabeceraMuestraCronometroBadgeYBotonMenu() {
        emitir(estadoJugando())
        regla.onNodeWithTag("cronometro_circular").assertIsDisplayed()
        regla.onNodeWithTag("badge_nivel").assertIsDisplayed()
        regla.onNodeWithText("Nivel 3").assertIsDisplayed()
        regla.onNodeWithTag("boton_menu_nivel").assertIsDisplayed()
    }

    @Test
    fun cuerpoMuestraPreguntaContadorYTresOpciones() {
        emitir(estadoJugando())
        regla.onNodeWithText("Nivel 3 de 20").assertIsDisplayed()
        regla.onNodeWithText("¿Cuánto es 6 × 7?").assertIsDisplayed()
        regla.onNodeWithTag("opcion_0").assertIsDisplayed()
        regla.onNodeWithTag("opcion_1").assertIsDisplayed()
        regla.onNodeWithTag("opcion_2").assertIsDisplayed()
        regla.onNodeWithText("42").assertIsDisplayed()
        regla.onNodeWithText("36").assertIsDisplayed()
        regla.onNodeWithText("49").assertIsDisplayed()
    }

    @Test
    fun clickEnOpcionPropagaElIndice() {
        var indiceClick: Int? = null
        emitir(estadoJugando(), onOpcionClick = { indiceClick = it })
        regla.onNodeWithTag("opcion_2").performClick()
        assertEquals(2, indiceClick)
    }

    @Test
    fun resultadoCorrectoDeshabilitaOpcionesYMuestraSheet() {
        emitir(
            estadoJugando().copy(
                fase = FaseJuego.RESULTADO,
                opcionSeleccionada = 0,
                resultadoCorrecto = true,
            ),
        )
        regla.onNodeWithTag("resultado_sheet").assertIsDisplayed()
        regla.onNodeWithText("¡Correcto!").assertIsDisplayed()
        regla.onNodeWithTag("opcion_0").assertIsNotEnabled()
        regla.onNodeWithTag("opcion_1").assertIsNotEnabled()
        regla.onNodeWithTag("opcion_2").assertIsNotEnabled()
    }

    @Test
    fun faseMenuMuestraOverlayConContinuarReiniciarYSalir() {
        emitir(estadoJugando().copy(fase = FaseJuego.MENU))
        regla.onNodeWithTag("overlay_menu_nivel").assertIsDisplayed()
        regla.onNodeWithTag("menu_continuar").assertIsDisplayed()
        regla.onNodeWithTag("menu_reiniciar").assertIsDisplayed()
        regla.onNodeWithTag("menu_salir").assertIsDisplayed()
        regla.onNodeWithText("Continuar").assertIsDisplayed()
        regla.onNodeWithText("Reiniciar").assertIsDisplayed()
        regla.onNodeWithText("Salir").assertIsDisplayed()
    }

    @Test
    fun fasePausaMuestraOverlayConMensajeYCuentaRegresiva() {
        emitir(estadoJugando().copy(fase = FaseJuego.PAUSA, segundosPausa = 45))
        regla.onNodeWithTag("overlay_pausa").assertIsDisplayed()
        regla.onNodeWithText("Estírate y respira.").assertIsDisplayed()
        regla.onNodeWithText("Tómate un momento.").assertIsDisplayed()
        regla.onNodeWithText("45").assertIsDisplayed()
    }

    private fun estadoJugando() = JuegoUiState(
        fase = FaseJuego.JUGANDO,
        nivel = NivelConProgreso(
            orden = 3,
            pregunta = "¿Cuánto es 6 × 7?",
            opcionA = "42",
            opcionB = "36",
            opcionC = "49",
            respuestaCorrecta = 0,
            estado = "DISPONIBLE",
            intentosTotales = 0,
            intentosFallidosConsecutivos = 0,
            completadoEn = null,
            sincronizado = true,
        ),
        segundosRestantes = 7,
    )
}
