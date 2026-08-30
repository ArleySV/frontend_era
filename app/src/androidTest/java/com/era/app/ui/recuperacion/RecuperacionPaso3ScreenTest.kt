package com.era.app.ui.recuperacion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.CriteriosContrasena
import com.era.app.utils.EraError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecuperacionPaso3ScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private fun criterios() = CriteriosContrasena(
        longitudMinima = false,
        tieneMayuscula = false,
        tieneMinuscula = false,
        tieneNumero = false,
        tieneSimbolo = false,
        distintaDeUsuario = true,
        sinDatosPersonales = true,
    )

    @Test
    fun muestraCamposDeContrasena() {
        regla.setContent {
            ERATheme {
                RecuperacionPaso3Content(
                    nuevaContrasena = "",
                    onNuevaContrasenaChange = {},
                    nuevaContrasenaError = null,
                    nuevaContrasenaVisible = false,
                    onNuevaContrasenaVisibleToggle = {},
                    confirmarContrasena = "",
                    onConfirmarContrasenaChange = {},
                    confirmarError = null,
                    confirmarVisible = false,
                    onConfirmarVisibleToggle = {},
                    criteriosContrasena = criterios(),
                    errorGeneral = null,
                    onGuardarContrasena = {},
                )
            }
        }
        regla.onNodeWithText("Nueva contraseña").assertIsDisplayed()
        regla.onNodeWithText("Confirmar contraseña").assertIsDisplayed()
        regla.onNodeWithText("Guardar contraseña").assertIsDisplayed()
    }

    @Test
    fun toggleOjoConmutaVisualTransformation() {
        regla.setContent {
            var visible by remember { mutableStateOf(false) }
            ERATheme {
                RecuperacionPaso3Content(
                    nuevaContrasena = "",
                    onNuevaContrasenaChange = {},
                    nuevaContrasenaError = null,
                    nuevaContrasenaVisible = visible,
                    onNuevaContrasenaVisibleToggle = { visible = !visible },
                    confirmarContrasena = "",
                    onConfirmarContrasenaChange = {},
                    confirmarError = null,
                    confirmarVisible = false,
                    onConfirmarVisibleToggle = {},
                    criteriosContrasena = criterios(),
                    errorGeneral = null,
                    onGuardarContrasena = {},
                )
            }
        }
        regla.onNode(
            hasContentDescription("Mostrar contraseña") and
                hasAnyAncestor(hasTestTag("campoNuevaContrasena"))
        ).performClick()
        regla.onNode(
            hasContentDescription("Ocultar contraseña") and
                hasAnyAncestor(hasTestTag("campoNuevaContrasena"))
        ).assertIsDisplayed()
    }

    @Test
    fun guardarContrasenaInvocaCallback() {
        var guarda = 0
        regla.setContent {
            ERATheme {
                RecuperacionPaso3Content(
                    nuevaContrasena = "",
                    onNuevaContrasenaChange = {},
                    nuevaContrasenaError = null,
                    nuevaContrasenaVisible = false,
                    onNuevaContrasenaVisibleToggle = {},
                    confirmarContrasena = "",
                    onConfirmarContrasenaChange = {},
                    confirmarError = null,
                    confirmarVisible = false,
                    onConfirmarVisibleToggle = {},
                    criteriosContrasena = criterios(),
                    errorGeneral = null,
                    onGuardarContrasena = { guarda++ },
                )
            }
        }
        regla.onNodeWithTag("botonGuardarContrasena").performClick()
        regla.runOnIdle { assertEquals(1, guarda) }
    }

    @Test
    fun error409PasswordReusadaSeMuestra() {
        regla.setContent {
            ERATheme {
                RecuperacionPaso3Content(
                    nuevaContrasena = "",
                    onNuevaContrasenaChange = {},
                    nuevaContrasenaError = null,
                    nuevaContrasenaVisible = false,
                    onNuevaContrasenaVisibleToggle = {},
                    confirmarContrasena = "",
                    onConfirmarContrasenaChange = {},
                    confirmarError = null,
                    confirmarVisible = false,
                    onConfirmarVisibleToggle = {},
                    criteriosContrasena = criterios(),
                    errorGeneral = EraError.PasswordReusada,
                    onGuardarContrasena = {},
                )
            }
        }
        regla.onNodeWithText("No puedes repetir tu contraseña anterior").assertIsDisplayed()
    }
}
