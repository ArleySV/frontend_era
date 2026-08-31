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
class MiCuentaViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeUserRepository
    private lateinit var sesion: FakeSesionRepository
    private lateinit var vm: MiCuentaViewModel

    private val perfil = UserProfile(
        nombreMenor = "María López",
        fechaNacimiento = "2016-05-10",
        correo = "acudiente@test.com",
        nombreUsuario = "maria_lopez",
        avatar = "preset:1",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeUserRepository()
        sesion = FakeSesionRepository()
        vm = MiCuentaViewModel(repo, sesion)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- onEntrar ----------

    @Test
    fun `onEntrar exito puebla perfil y apaga cargando`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))

        vm.onEntrar()
        advanceUntilIdle()

        assertEquals(perfil, vm.uiState.value.perfil)
        assertFalse(vm.uiState.value.cargando)
        assertNull(vm.uiState.value.errorGeneral)
    }

    @Test
    fun `GET ErrorConexion setea errorGeneral`() = runTest {
        repo.encolarObtener(Resultado.Fallo(EraError.ErrorConexion))

        vm.onEntrar()
        advanceUntilIdle()

        assertEquals(EraError.ErrorConexion, vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun `GET CuentaInactiva limpia token y emite NavegarALogin`() = runTest {
        sesion.tokenGuardado = "token_previo"
        repo.encolarObtener(Resultado.Fallo(EraError.CuentaInactiva))
        val eventos = mutableListOf<MiCuentaEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onEntrar()
        advanceUntilIdle()

        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is MiCuentaEvento.NavegarALogin })
    }

    @Test
    fun `GET SesionExpirada limpia token y emite NavegarALogin`() = runTest {
        sesion.tokenGuardado = "token_previo"
        repo.encolarObtener(Resultado.Fallo(EraError.SesionExpirada))
        val eventos = mutableListOf<MiCuentaEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onEntrar()
        advanceUntilIdle()

        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is MiCuentaEvento.NavegarALogin })
    }

    @Test
    fun `GET PerfilNoEncontrado setea errorGeneral`() = runTest {
        repo.encolarObtener(Resultado.Fallo(EraError.PerfilNoEncontrado))

        vm.onEntrar()
        advanceUntilIdle()

        assertEquals(EraError.PerfilNoEncontrado, vm.uiState.value.errorGeneral)
    }

    // ---------- Dialog ----------

    @Test
    fun `onEditarClick abre dialog y precarga username actual`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))
        vm.onEntrar()
        advanceUntilIdle()

        vm.onEditarClick()

        assertTrue(vm.uiState.value.dialogoAbierto)
        assertEquals("maria_lopez", vm.uiState.value.nombreUsuario)
    }

    @Test
    fun `onDialogCancelar cierra dialog y limpia el campo`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))
        vm.onEntrar()
        advanceUntilIdle()
        vm.onEditarClick()
        vm.onNombreUsuarioChange("cancelar_test")

        vm.onDialogCancelar()

        assertFalse(vm.uiState.value.dialogoAbierto)
        assertEquals("", vm.uiState.value.nombreUsuario)
        assertNull(vm.uiState.value.errorNombreUsuario)
    }

    // ---------- Guardar ----------

    @Test
    fun `guardar username invalido muestra error inline sin llamar a la red`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))
        vm.onEntrar()
        advanceUntilIdle()
        vm.onEditarClick()
        vm.onNombreUsuarioChange("ab")

        vm.onGuardarClick()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errorNombreUsuario == "3-60 caracteres, sin espacios")
        assertTrue(vm.uiState.value.dialogoAbierto)
        assertTrue(repo.llamadasActualizar.isEmpty())
    }

    @Test
    fun `guardar exito actualiza perfil y cierra dialog`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))
        vm.onEntrar()
        advanceUntilIdle()
        vm.onEditarClick()
        val perfilActualizado = perfil.copy(nombreUsuario = "maria_nueva")
        repo.encolarActualizar(Resultado.Exito(perfilActualizado))

        vm.onNombreUsuarioChange("maria_nueva")
        vm.onGuardarClick()
        advanceUntilIdle()

        assertEquals("maria_nueva", vm.uiState.value.perfil?.nombreUsuario)
        assertFalse(vm.uiState.value.dialogoAbierto)
        assertFalse(vm.uiState.value.guardando)
    }

    @Test
    fun `guardar con 409 UsuarioEnUso muestra error inline y mantiene dialog abierto`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))
        vm.onEntrar()
        advanceUntilIdle()
        vm.onEditarClick()
        repo.encolarActualizar(Resultado.Fallo(EraError.UsuarioEnUso))

        vm.onNombreUsuarioChange("en_uso")
        vm.onGuardarClick()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.dialogoAbierto)
        assertEquals("Este nombre de usuario ya está en uso", vm.uiState.value.errorNombreUsuario)
        assertFalse(vm.uiState.value.guardando)
    }

    @Test
    fun `guardar con CuentaInactiva limpia token y emite NavegarALogin`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))
        vm.onEntrar()
        advanceUntilIdle()
        vm.onEditarClick()
        sesion.tokenGuardado = "token_previo"
        repo.encolarActualizar(Resultado.Fallo(EraError.CuentaInactiva))
        val eventos = mutableListOf<MiCuentaEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onNombreUsuarioChange("nuevo")
        vm.onGuardarClick()
        advanceUntilIdle()

        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is MiCuentaEvento.NavegarALogin })
    }

    @Test
    fun `guardar con ErrorServidor no navega y envia snackbar`() = runTest {
        repo.encolarObtener(Resultado.Exito(perfil))
        vm.onEntrar()
        advanceUntilIdle()
        vm.onEditarClick()
        repo.encolarActualizar(Resultado.Fallo(EraError.ErrorServidor))
        val eventos = mutableListOf<MiCuentaEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onNombreUsuarioChange("nuevo")
        vm.onGuardarClick()
        advanceUntilIdle()

        job.cancel()
        assertTrue(vm.uiState.value.dialogoAbierto)
        assertFalse(vm.uiState.value.guardando)
        assertTrue(eventos.any { it is MiCuentaEvento.MostrarSnackbar })
        assertFalse(eventos.any { it is MiCuentaEvento.NavegarALogin })
    }

    // ---------- Fakes ----------

    private class FakeUserRepository : UserRepository {
        val llamadasActualizar = mutableListOf<String>()
        private val obtenerQueue = ArrayDeque<Resultado<UserProfile>>()
        private val actualizarQueue = ArrayDeque<Resultado<UserProfile>>()

        fun encolarObtener(respuesta: Resultado<UserProfile>) { obtenerQueue += respuesta }
        fun encolarActualizar(respuesta: Resultado<UserProfile>) { actualizarQueue += respuesta }

        override suspend fun obtenerPerfil(): Resultado<UserProfile> =
            obtenerQueue.removeFirstOrNull() ?: error("obtenerPerfil llamado sin respuesta encolada")

        override suspend fun actualizarNombreUsuario(nombre: String): Resultado<UserProfile> {
            llamadasActualizar += nombre
            return actualizarQueue.removeFirstOrNull()
                ?: error("actualizarNombreUsuario llamado sin respuesta encolada")
        }

        override suspend fun eliminarCuenta(contrasena: String): Resultado<Unit> = error("No usado")
    }

    private class FakeSesionRepository : SesionRepository {
        var tokenGuardado: String? = null
        var fueLimpiado = false

        override fun guardarToken(token: String) { tokenGuardado = token }
        override fun obtenerToken(): String? = tokenGuardado
        override fun guardarCorreo(correo: String) {}
        override fun obtenerCorreo(): String? = null
        override fun limpiarToken() { tokenGuardado = null; fueLimpiado = true }
        override fun tieneToken(): Boolean = tokenGuardado != null
    }
}
