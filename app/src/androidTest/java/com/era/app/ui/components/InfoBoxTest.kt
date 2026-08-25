package com.era.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InfoBoxTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraElTextoInformativo() {
        val mensaje = "La contraseña no puede contener tu nombre"
        regla.setContent {
            ERATheme {
                InfoBox(texto = mensaje, modifier = Modifier.padding(16.dp))
            }
        }
        regla.onNodeWithText(mensaje).assertIsDisplayed()
    }
}
