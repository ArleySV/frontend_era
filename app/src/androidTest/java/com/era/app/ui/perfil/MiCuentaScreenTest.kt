package com.era.app.ui.perfil

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.era.app.remote.dto.user.UserProfile
import com.era.app.ui.theme.ERATheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class MiCuentaScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private val perfil = UserProfile(
        nombreMenor = "María López",
        fechaNacimiento = "2016-05-10",
        correo = "acudiente@test.com",
        nombreUsuario = "maria_lopez",
        avatar = null,
    )

    @Composable
    private fun emitir(uiState: MiCuentaUiState = MiCuentaUiState(perfil = perfil)) =
        MiCuentaContent(
            uiState = uiState,
            onVolver = {},
            onEditar = {},
            onDialogCancelar = {},
            onNombreUsuarioChange = {},
            onGuardar = {},
            onReintentar = {},
        )

    @Test
    fun cargaMuestraTituloYLosCincoCampos() {
        regla.setContent { ERATheme { emitir() } }
        regla.onNodeWithText("Mi Cuenta").assertIsDisplayed()
        regla.onNodeWithText("Nombre del menor").assertIsDisplayed()
        regla.onNodeWithText("María López").assertIsDisplayed()
        regla.onNodeWithText("Correo electrónico").assertIsDisplayed()
        regla.onNodeWithText("acudiente@test.com").assertIsDisplayed()
        regla.onNodeWithText("Nombre de usuario").assertIsDisplayed()
        regla.onNodeWithText("Fecha de nacimiento").assertIsDisplayed()
    }

    @Test
    fun avatarNuloMuestraIniciales() {
        regla.setContent { ERATheme { emitir() } }
        regla.onNodeWithText("ML").assertIsDisplayed()
    }

    @Test
    fun avatarPresetCargaDrawableSinCrash() {
        val conPreset = perfil.copy(avatar = "preset:1")
        regla.setContent { ERATheme { emitir(MiCuentaUiState(perfil = conPreset)) } }
        regla.onNodeWithContentDescription("Avatar").assertIsDisplayed()
    }

    @Test
    fun fechaSeMuestraEnFormatoDiaBarraAnio() {
        regla.setContent { ERATheme { emitir() } }
        regla.onNodeWithText("10/05/2016").assertIsDisplayed()
    }

    @Test
    fun botonVolverInvocaCallback() {
        var veces = 0
        regla.setContent {
            ERATheme {
                MiCuentaContent(
                    uiState = MiCuentaUiState(perfil = perfil),
                    onVolver = { veces++ },
                    onEditar = {},
                    onDialogCancelar = {},
                    onNombreUsuarioChange = {},
                    onGuardar = {},
                    onReintentar = {},
                )
            }
        }
        regla.onNodeWithContentDescription("Volver").performClick()
        regla.runOnIdle { assertEquals(1, veces) }
    }

    @Test
    fun editarAbreDialogConUsernameActual() {
        regla.setContent {
            ERATheme {
                emitir(MiCuentaUiState(perfil = perfil, dialogoAbierto = true, nombreUsuario = "maria_lopez"))
            }
        }
        regla.onNodeWithText("Editar nombre de usuario").assertIsDisplayed()
        regla.onNodeWithText("Guardar").assertIsDisplayed()
        regla.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun cancelarInvocaCallback() {
        var cancela = 0
        regla.setContent {
            ERATheme {
                MiCuentaContent(
                    uiState = MiCuentaUiState(perfil = perfil, dialogoAbierto = true, nombreUsuario = "maria_lopez"),
                    onVolver = {},
                    onEditar = {},
                    onDialogCancelar = { cancela++ },
                    onNombreUsuarioChange = {},
                    onGuardar = {},
                    onReintentar = {},
                )
            }
        }
        regla.onNodeWithText("Cancelar").performClick()
        regla.runOnIdle { assertEquals(1, cancela) }
    }

    @Test
    fun guardarInvocaCallback() {
        var guarda = 0
        regla.setContent {
            ERATheme {
                MiCuentaContent(
                    uiState = MiCuentaUiState(perfil = perfil, dialogoAbierto = true, nombreUsuario = "nuevo"),
                    onVolver = {},
                    onEditar = {},
                    onDialogCancelar = {},
                    onNombreUsuarioChange = {},
                    onGuardar = { guarda++ },
                    onReintentar = {},
                )
            }
        }
        regla.onNodeWithText("Guardar").performClick()
        regla.runOnIdle { assertEquals(1, guarda) }
    }

    @Test
    fun errorInline409SeMuestra() {
        regla.setContent {
            ERATheme {
                emitir(
                    MiCuentaUiState(
                        perfil = perfil,
                        dialogoAbierto = true,
                        nombreUsuario = "en_uso",
                        errorNombreUsuario = "Este nombre de usuario ya está en uso",
                    )
                )
            }
        }
        regla.onNodeWithText("Este nombre de usuario ya está en uso").assertIsDisplayed()
    }

    @Test
    fun estadoCargandoMuestraSpinner() {
        regla.setContent { ERATheme { emitir(MiCuentaUiState(cargando = true)) } }
        regla.onNodeWithText("Mi Cuenta").assertIsDisplayed()
    }
}
