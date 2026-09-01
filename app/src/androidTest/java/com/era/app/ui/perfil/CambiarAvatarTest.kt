package com.era.app.ui.perfil

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
class CambiarAvatarTest {

    @get:Rule
    val regla = createComposeRule()

    private val perfil = UserProfile(
        nombreMenor = "María López",
        fechaNacimiento = "2016-05-10",
        correo = "acudiente@test.com",
        nombreUsuario = "maria_lopez",
        avatar = "preset:1",
    )

    @Composable
    private fun emitir(
        uiState: MiCuentaUiState = MiCuentaUiState(perfil = perfil),
        onCambiarAvatar: () -> Unit = {},
        onSeleccionarPreset: (Int) -> Unit = {},
        onSubirFoto: () -> Unit = {},
    ) = MiCuentaContent(
        uiState = uiState,
        onVolver = {},
        onEditar = {},
        onDialogCancelar = {},
        onNombreUsuarioChange = {},
        onGuardar = {},
        onReintentar = {},
        onEliminarCuenta = {},
        onCambiarAvatar = onCambiarAvatar,
        onSeleccionarPreset = onSeleccionarPreset,
        onSubirFoto = onSubirFoto,
    )

    @Test
    fun avatarSeMuestraConPerfilCargado() {
        regla.setContent { ERATheme { emitir() } }
        regla.onNodeWithTag("avatarTrigger").assertIsDisplayed()
    }

    @Test
    fun avatarNoSeMuestraSinPerfil() {
        regla.setContent { ERATheme { emitir(MiCuentaUiState()) } }
        regla.onNodeWithTag("avatarTrigger").assertDoesNotExist()
    }

    @Test
    fun avatarInvocaCallbackAlPulsar() {
        var veces = 0
        regla.setContent { ERATheme { emitir(onCambiarAvatar = { veces++ }) } }
        regla.onNodeWithTag("avatarTrigger").performClick()
        regla.runOnIdle { assertEquals(1, veces) }
    }

    @Test
    fun selectorAbiertoMuestraPresetsYBotonMas() {
        regla.setContent { ERATheme { emitir(MiCuentaUiState(perfil = perfil, selectorAvatarAbierto = true)) } }
        regla.onNodeWithContentDescription("Selector de avatar 1").assertIsDisplayed()
        regla.onNodeWithContentDescription("Subir avatar").assertIsDisplayed()
    }

    @Test
    fun seleccionarPresetInvocaCallback() {
        var seleccionado: Int? = null
        regla.setContent {
            ERATheme {
                emitir(
                    MiCuentaUiState(perfil = perfil, selectorAvatarAbierto = true),
                    onSeleccionarPreset = { seleccionado = it },
                )
            }
        }
        regla.onNodeWithContentDescription("Selector de avatar 2").performClick()
        regla.runOnIdle { assertEquals(2, seleccionado) }
    }

    @Test
    fun botonMasInvocaCallbackDeSubida() {
        var sube = 0
        regla.setContent {
            ERATheme {
                emitir(
                    MiCuentaUiState(perfil = perfil, selectorAvatarAbierto = true),
                    onSubirFoto = { sube++ },
                )
            }
        }
        regla.onNodeWithContentDescription("Subir avatar").performClick()
        regla.runOnIdle { assertEquals(1, sube) }
    }

    @Test
    fun avatarCustomDibujaAsyncImageConBytesSinCrash() {
        regla.setContent {
            ERATheme {
                emitir(
                    MiCuentaUiState(
                        perfil = perfil.copy(avatar = "custom:abc.png"),
                        bytesAvatarPersonalizado = byteArrayOf(1, 2, 3),
                    ),
                )
            }
        }
        regla.onNodeWithContentDescription("Avatar").assertIsDisplayed()
        regla.onNodeWithText("ML").assertDoesNotExist()
    }
}
