package com.era.app.ui.register

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.era.app.ui.theme.ERATheme
import com.era.app.utils.CriteriosContrasena
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class RegistroPaso2ScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Test
    fun muestraAvatarPlaceholderYInfoBox() {
        regla.setContent {
            ERATheme {
                RegistroPaso2Content(
                    correo = "",
                    onCorreoChange = {},
                    correoError = null,
                    nombreUsuario = "",
                    onNombreUsuarioChange = {},
                    nombreUsuarioError = null,
                    avatarSeleccionado = null,
                    onAvatarSeleccionar = {},
                    avatarError = null,
                    contrasena = "",
                    onContrasenaChange = {},
                    contrasenaError = null,
                    contrasenaVisible = false,
                    onContrasenaVisibleChange = {},
                    confirmarContrasena = "",
                    onConfirmarContrasenaChange = {},
                    confirmarError = null,
                    confirmarVisible = false,
                    onConfirmarVisibleChange = {},
                    criteriosContrasena = CriteriosContrasena(
                        longitudMinima = false,
                        tieneMayuscula = false,
                        tieneMinuscula = false,
                        tieneNumero = false,
                        tieneSimbolo = false,
                        distintaDeUsuario = true,
                        sinDatosPersonales = true,
                    ),
                    onAtras = {},
                    onContinuar = {},
                )
            }
        }
        regla.onNodeWithText("Elige un avatar").assertIsDisplayed()
        regla.onNodeWithText(
            "La contraseña no puede contener tu nombre, " +
                "ser una palabra del diccionario ni ser igual al usuario"
        ).assertIsDisplayed()
    }
}
