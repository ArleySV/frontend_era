package com.era.app.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompactGreenHeaderTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraTituloYSubtitulo() {
        regla.setContent {
            ERATheme {
                CompactGreenHeader(titulo = "Registro - Paso 1 de 3", subtitulo = "Datos de usuario")
            }
        }
        regla.onNodeWithText("Registro - Paso 1 de 3").assertExists()
        regla.onNodeWithText("Datos de usuario").assertExists()
    }
}
