package com.era.app.ui.login

import com.era.app.remote.dto.auth.LoginRequest
import com.era.app.remote.dto.auth.LoginResponse
import com.era.app.remote.dto.auth.PasswordResetConfirmRequest
import com.era.app.remote.dto.auth.PasswordResetRequest
import com.era.app.remote.dto.auth.PasswordResetVerifyRequest
import com.era.app.remote.dto.auth.PasswordResetVerifyResponse
import com.era.app.remote.dto.auth.RegisterRequest
import com.era.app.remote.dto.auth.ResendOtpRequest
import com.era.app.remote.dto.auth.VerifyEmailRequest
import com.era.app.remote.dto.common.MessageResponse
import com.era.app.repository.AuthRepository
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.utils.EraError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomePlaceholderViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var sesion: FakeSesionRepository
    private lateinit var vm: HomePlaceholderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        sesion = FakeSesionRepository()
        vm = HomePlaceholderViewModel(repo, sesion)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial con dialogo cerrado y sin cierre en vuelo`() {
        val s = vm.uiState.value
        assertFalse(s.dialogoCierreVisible)
        assertFalse(s.cerrando)
    }

    @Test
    fun `onCerrarSesionClick abre el dialogo de confirmacion`() {
        vm.onCerrarSesionClick()
        assertTrue(vm.uiState.value.dialogoCierreVisible)
        assertFalse(vm.uiState.value.cerrando)
    }

    @Test
    fun `onCancelarCierre cierra el dialogo sin llamar red ni limpiar token`() = runTest {
        vm.onCerrarSesionClick()
        vm.onCancelarCierre()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.dialogoCierreVisible)
        assertTrue(repo.llamadas.isEmpty())
        assertFalse(sesion.fueLimpiado)
    }

    @Test
    fun `confirmar con logout 200 limpia token y navega a login`() = runTest {
        repo.encolarLogout(Resultado.Exito(MessageResponse(message = "Sesión cerrada.")))
        val eventos = mutableListOf<HomePlaceholderEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onCerrarSesionClick()
        vm.onConfirmarCierre()
        advanceUntilIdle()

        job.cancel()
        assertEquals(listOf("logout"), repo.llamadas)
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is HomePlaceholderEvento.NavegarALogin })
        assertFalse(vm.uiState.value.cerrando)
        assertFalse(vm.uiState.value.dialogoCierreVisible)
    }

    @Test
    fun `confirmar offline limpia token y navega a login igual`() = runTest {
        repo.encolarLogout(Resultado.Fallo(EraError.ErrorConexion))
        val eventos = mutableListOf<HomePlaceholderEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onCerrarSesionClick()
        vm.onConfirmarCierre()
        advanceUntilIdle()

        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is HomePlaceholderEvento.NavegarALogin })
    }

    @Test
    fun `confirmar con token ya invalido limpia y navega a login`() = runTest {
        repo.encolarLogout(Resultado.Fallo(EraError.SesionExpirada))
        val eventos = mutableListOf<HomePlaceholderEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onCerrarSesionClick()
        vm.onConfirmarCierre()
        advanceUntilIdle()

        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is HomePlaceholderEvento.NavegarALogin })
    }

    @Test
    fun `confirmar con error de servidor limpia y navega a login`() = runTest {
        repo.encolarLogout(Resultado.Fallo(EraError.ErrorServidor))
        val eventos = mutableListOf<HomePlaceholderEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onCerrarSesionClick()
        vm.onConfirmarCierre()
        advanceUntilIdle()

        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is HomePlaceholderEvento.NavegarALogin })
    }

    @Test
    fun `cancelar no interrumpe un cierre en vuelo`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repo = FakeAuthRepository(beforeLogout = { gate.await() })
        repo.encolarLogout(Resultado.Exito(MessageResponse(message = "Sesión cerrada.")))
        vm = HomePlaceholderViewModel(repo, sesion)
        val eventos = mutableListOf<HomePlaceholderEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onCerrarSesionClick()
        vm.onConfirmarCierre()
        runCurrent()

        assertTrue(vm.uiState.value.cerrando)
        vm.onCancelarCierre()
        assertTrue(vm.uiState.value.dialogoCierreVisible)
        assertTrue(vm.uiState.value.cerrando)

        gate.complete(Unit)
        advanceUntilIdle()
        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is HomePlaceholderEvento.NavegarALogin })
        assertFalse(vm.uiState.value.dialogoCierreVisible)
    }

    @Test
    fun `doble confirmacion llama a logout una sola vez`() = runTest {
        repo.encolarLogout(Resultado.Exito(MessageResponse(message = "Sesión cerrada.")))

        vm.onCerrarSesionClick()
        vm.onConfirmarCierre()
        vm.onConfirmarCierre()
        advanceUntilIdle()

        assertEquals(1, repo.llamadas.count { it == "logout" })
        assertTrue(sesion.fueLimpiado)
    }

    @Test
    fun `onCerrarSesionClick con dialogo ya abierto no cambia estado`() {
        vm.onCerrarSesionClick()
        vm.onCerrarSesionClick()

        assertTrue(vm.uiState.value.dialogoCierreVisible)
        assertFalse(vm.uiState.value.cerrando)
        assertTrue(repo.llamadas.isEmpty())
    }

    // ---------- Fakes ----------

    private class FakeAuthRepository(
        private val beforeLogout: suspend () -> Unit = {},
    ) : AuthRepository {
        val llamadas = mutableListOf<String>()
        private val logoutQueue = ArrayDeque<Resultado<MessageResponse>>()

        fun encolarLogout(respuesta: Resultado<MessageResponse>) { logoutQueue += respuesta }

        override suspend fun register(request: RegisterRequest): Resultado<Unit> =
            error("No usado en home placeholder tests")

        override suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit> =
            error("No usado en home placeholder tests")

        override suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit> =
            error("No usado en home placeholder tests")

        override suspend fun login(request: LoginRequest): Resultado<LoginResponse> =
            error("No usado en home placeholder tests")

        override suspend fun logout(): Resultado<MessageResponse> {
            llamadas += "logout"
            beforeLogout()
            return logoutQueue.removeFirstOrNull()
                ?: error("logout llamado sin respuesta encolada")
        }

        override suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit> =
            error("No usado en home placeholder tests")

        override suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse> =
            error("No usado en home placeholder tests")

        override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit> =
            error("No usado en home placeholder tests")
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