package com.era.app.ui.faq

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.era.app.data.model.FaqItem
import com.era.app.ui.theme.ERATheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class FaqScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private val faqEjemplo = listOf(
        FaqItem(1, "¿Pregunta 1?", "Respuesta 1"),
        FaqItem(2, "¿Pregunta 2?", "Respuesta 2")
    )

    @Test
    fun muestraTituloYFaqs() {
        regla.setContent {
            ERATheme {
                FaqContent(
                    uiState = FaqUiState(faqs = faqEjemplo),
                    onVolver = {},
                    onComentarioChange = {},
                    onEnviarComentario = {}
                )
            }
        }
        regla.onNodeWithText("Ayuda y Comentarios").assertIsDisplayed()
        regla.onNodeWithText("Preguntas Frecuentes").assertIsDisplayed()
        regla.onNodeWithText("¿Pregunta 1?").assertIsDisplayed()
        regla.onNodeWithText("¿Pregunta 2?").assertIsDisplayed()
    }

    @Test
    fun expandeYContraeFaq() {
        regla.setContent {
            ERATheme {
                FaqContent(
                    uiState = FaqUiState(faqs = faqEjemplo),
                    onVolver = {},
                    onComentarioChange = {},
                    onEnviarComentario = {}
                )
            }
        }
        // Inicialmente no visible (unmerged porque está colapsado y podría no existir o estar oculto)
        regla.onNodeWithTag("faqRespuesta_1", useUnmergedTree = true).assertDoesNotExist()
        
        // Expandir
        regla.onNodeWithTag("faqCard_1").performClick()
        
        // Esperar a que la respuesta exista en el árbol
        regla.onNodeWithTag("faqRespuesta_1", useUnmergedTree = true).assertExists()
        regla.onNodeWithText("Respuesta 1").assertIsDisplayed()
        
        // Contraer
        regla.onNodeWithTag("faqCard_1").performClick()
        regla.onNodeWithTag("faqRespuesta_1", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun campoComentarioActualizaContadorYHabilitaBoton() {
        regla.setContent {
            var texto by remember { mutableStateOf("") }
            ERATheme {
                FaqContent(
                    uiState = FaqUiState(comentario = texto),
                    onVolver = {},
                    onComentarioChange = { texto = it },
                    onEnviarComentario = {}
                )
            }
        }

        // Target actual text field inside the EraTextField component
        val input = regla.onNode(hasSetTextAction())
        input.performTextInput("Sugerencia")
        
        regla.onNodeWithText("10/2000").assertIsDisplayed()
        regla.onNodeWithTag("botonEnviarComentario").assertIsEnabled()
    }

    @Test
    fun botonEnviarMuestraCargando() {
        regla.setContent {
            ERATheme {
                FaqContent(
                    uiState = FaqUiState(enviandoComentario = true, comentario = "Hola"),
                    onVolver = {},
                    onComentarioChange = {},
                    onEnviarComentario = {}
                )
            }
        }
        regla.onNodeWithTag("cargandoEnvio", useUnmergedTree = true).assertIsDisplayed()
        regla.onNodeWithTag("botonEnviarComentario").assertIsNotEnabled()
    }
}
