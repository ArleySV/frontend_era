package com.era.app.ui.perfil

import com.era.app.remote.dto.user.UserProfile
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.repository.UserRepository
import com.era.app.utils.EraError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EliminarCuentaViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeUserRepository
    private lateinit var sesion: FakeSesionRepository
    private lateinit var vm: EliminarCuentaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeUserRepository()
        sesion = FakeSesionRepository()
        vm = EliminarCuentaViewModel(repo, sesion)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial correcto`() {
        val state = vm.uiState.value
        assertEquals("", state.contrasena)
        assertFalse(state.contrasenaVisible)
        assertFalse(state.cargando)
        assertNull(state.errorGeneral)
        assertFalse(state.mostrarDialogoConfirmacion)
    }

    @Test
    fun `onContrasenaChange actualiza valor y limpia error`() {
        vm.onContrasenaChange("nueva_pass")
        assertEquals("nueva_pass", vm.uiState.value.contrasena)
    }

    @Test
    fun `onToggleContrasenaVisible cambia visibilidad`() {
        vm.onToggleContrasenaVisible()
        assertTrue(vm.uiState.value.contrasenaVisible)
        vm.onToggleContrasenaVisible()
        assertFalse(vm.uiState.value.contrasenaVisible)
    }

    @Test
    fun `onEliminarClick con contrasena vacia no hace nada`() {
        vm.onEliminarClick()
        assertFalse(vm.uiState.value.mostrarDialogoConfirmacion)
    }

    @Test
    fun `onEliminarClick con contrasena muestra dialogo`() {
        vm.onContrasenaChange("pass")
        vm.onEliminarClick()
        assertTrue(vm.uiState.value.mostrarDialogoConfirmacion)
    }

    @Test
    fun `onDismissDialog cierra dialogo`() {
        vm.onContrasenaChange("pass")
        vm.onEliminarClick()
        vm.onDismissDialog()
        assertFalse(vm.uiState.value.mostrarDialogoConfirmacion)
    }

    @Test
    fun `confirmarEliminacion exito limpia token y emite NavegarALogin`() = runTest {
        repo.encolarEliminar(Resultado.Exito(Unit))
        val eventos = mutableListOf<EliminarCuentaEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onContrasenaChange("pass")
        vm.confirmarEliminacion()
        advanceUntilIdle()

        job.cancel()
        assertTrue("Sesión debería estar limpia", sesion.fueLimpiado)
        assertTrue("Evento NavegarALogin debería haberse emitido", eventos.any { it is EliminarCuentaEvento.NavegarALogin })
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun `confirmarEliminacion fallo setea errorGeneral`() = runTest {
        repo.encolarEliminar(Resultado.Fallo(EraError.CredencialesInvalidas))
        vm.onContrasenaChange("wrong")

        vm.confirmarEliminacion()
        advanceUntilIdle()

        assertEquals(EraError.CredencialesInvalidas, vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.cargando)
    }

    // ---------- Fakes ----------

    private class FakeUserRepository : UserRepository {
        private val eliminarQueue = ArrayDeque<Resultado<Unit>>()

        fun encolarEliminar(respuesta: Resultado<Unit>) { eliminarQueue += respuesta }

        override suspend fun obtenerPerfil(): Resultado<UserProfile> = error("No usado")
        override suspend fun actualizarNombreUsuario(nombre: String): Resultado<UserProfile> = error("No usado")

        override suspend fun eliminarCuenta(contrasena: String): Resultado<Unit> {
            return eliminarQueue.removeFirstOrNull() ?: error("eliminarCuenta llamado sin respuesta encolada")
        }
    }

    private class FakeSesionRepository : SesionRepository {
        var fueLimpiado = false
        override fun guardarToken(token: String) {}
        override fun obtenerToken(): String? = null
        override fun limpiarToken() { fueLimpiado = true }
        override fun tieneToken(): Boolean = false
    }
}
