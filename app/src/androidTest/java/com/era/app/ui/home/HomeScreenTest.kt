package com.era.app.ui.home

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.era.app.ui.components.EraIcons
import com.era.app.ui.components.layout.EraDrawer
import com.era.app.ui.components.layout.EraDrawerItem
import com.era.app.ui.theme.ERATheme
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val regla = createComposeRule()

    @Composable
    private fun emitir(
        nombre: String,
        correo: String,
        avatar: String? = null,
        bytesAvatarCustom: ByteArray? = null,
        cargandoPerfil: Boolean = false,
        onNavegarNiveles: () -> Unit = {},
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val items = listOf(
            EraDrawerItem("perfil", EraIcons.AccountCircle, "Mi cuenta"),
            EraDrawerItem("progreso", EraIcons.Assessment, "Progreso"),
            EraDrawerItem("separador", EraIcons.Assessment, ""),
            EraDrawerItem("ajustes", EraIcons.Settings, "Ajustes", habilitado = false),
            EraDrawerItem("faq", EraIcons.Help, "FAQ"),
            EraDrawerItem("separador", EraIcons.Assessment, ""),
            EraDrawerItem("cerrar_sesion", EraIcons.Logout, "Cerrar sesión"),
        )
        ERATheme {
            EraDrawer(
                drawerState = drawerState,
                nombre = nombre,
                correo = correo,
                avatar = avatar,
                cargandoPerfil = cargandoPerfil,
                items = items,
                onItemClick = {},
                onCerrarSesionClick = {},
                bytesAvatarCustom = bytesAvatarCustom,
            ) {
                HomeScreen(
                    nombreMenor = nombre,
                    cargandoPerfil = cargandoPerfil,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavegarNiveles = onNavegarNiveles,
                )
            }
        }
    }

    @Test
    fun saludoMuestraNombreYFrase() {
        regla.setContent { emitir(nombre = "Sebastián", correo = "acu@test.com") }
        regla.onNodeWithText("¡Hola, Sebastián!").assertIsDisplayed()
        regla.onNodeWithText("Nos alegra tenerte de nuevo por aquí").assertIsDisplayed()
    }

    @Test
    fun hamburguesaAbreElDrawerConItems() {
        regla.setContent { emitir(nombre = "Sebastián", correo = "acu@test.com") }
        regla.onNodeWithTag("boton_hamburguesa").performClick()
        regla.onNodeWithText("ERA - Educación, Repaso y Aprendizaje").assertIsDisplayed()
        regla.onNodeWithText("Mi cuenta").assertIsDisplayed()
        regla.onNodeWithText("Progreso").assertIsDisplayed()
        regla.onNodeWithText("FAQ").assertIsDisplayed()
        regla.onNodeWithText("Cerrar sesión").assertIsDisplayed()
    }

    @Test
    fun drawerMuestraCorreoYNombreDelMenor() {
        regla.setContent { emitir(nombre = "Sebastián", correo = "acu@test.com") }
        regla.onNodeWithTag("boton_hamburguesa").performClick()
        regla.onNodeWithText("Sebastián").assertIsDisplayed()
        regla.onNodeWithText("acu@test.com").assertIsDisplayed()
    }

    @Test
    fun itemAjustesDeshabilitadoEnS2() {
        regla.setContent { emitir(nombre = "Sebastián", correo = "acu@test.com") }
        regla.onNodeWithTag("boton_hamburguesa").performClick()
        regla.onNodeWithTag("drawer_item_ajustes").assertIsNotEnabled()
    }

    @Test
    fun avatarConUrlValidaMuestraImagenYNoIniciales() {
        regla.setContent {
            emitir(
                nombre = "Judith Salcedo",
                correo = "hevine5742@ehwit.com",
                avatar = "https://backend.example.com/api/v1/users/me/avatar",
            )
        }
        regla.onNodeWithTag("boton_hamburguesa").performClick()
        regla.onNodeWithContentDescription("Avatar").assertIsDisplayed()
        regla.onNodeWithText("JS").assertDoesNotExist()
    }

    @Test
    fun avatarCustomConBytesMuestraImagen() {
        regla.setContent {
            emitir(
                nombre = "Judith Salcedo",
                correo = "hevine5742@ehwit.com",
                avatar = "custom:1",
                bytesAvatarCustom = byteArrayOf(1, 2, 3),
            )
        }
        regla.onNodeWithTag("boton_hamburguesa").performClick()
        regla.onNodeWithContentDescription("Avatar").assertIsDisplayed()
        regla.onNodeWithText("JS").assertDoesNotExist()
    }

    @Test
    fun avatarNullMuestraIniciales() {
        regla.setContent { emitir(nombre = "Sebastián Torres", correo = "acu@test.com") }
        regla.onNodeWithTag("boton_hamburguesa").performClick()
        regla.onNodeWithText("ST").assertIsDisplayed()
    }

    @Test
    fun headerMantieneAltoMinimoConNombreCorto() {
        regla.setContent { emitir(nombre = "E", correo = "e@e.com") }
        regla.onNodeWithTag("boton_hamburguesa").performClick()
        val nodo = regla.onNodeWithTag("drawer_cabecera").fetchSemanticsNode()
        val densidad = androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .resources
            .displayMetrics
            .density
        val minAltoPx = 220f * densidad
        assertTrue(
            "Alto de cabecera ${nodo.size.height}px < mínimo ${minAltoPx.toInt()}px",
            nodo.size.height >= minAltoPx,
        )
    }
}
