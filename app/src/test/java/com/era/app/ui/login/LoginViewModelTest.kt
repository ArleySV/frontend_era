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
import com.era.app.repository.AuthRepository
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
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
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var sesion: FakeSesionRepository
    private lateinit var vm: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        sesion = FakeSesionRepository()
        vm = LoginViewModel(repo, sesion)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial tiene campos vacios y cargando falso`() {
        val s = vm.uiState.value
        assertEquals("", s.usuarioOCorreo)
        assertEquals("", s.contrasena)
        assertFalse(s.contrasenaVisible)
        assertFalse(s.cargando)
        assertNull(s.errorGeneral)
        assertNull(s.campoConError)
    }

    @Test
    fun `onLoginClick con campos vacios no llama al repositorio`() = runTest {
        vm.onLoginClick()
        advanceUntilIdle()
        assertTrue(repo.llamadas.isEmpty())
    }

    @Test
    fun `onLoginClick con usuario vacio marca campoConError USUARIO_O_CORREO`() = runTest {
        vm.onContrasenaChange("pass123")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals(CampoLogin.USUARIO_O_CORREO, vm.uiState.value.campoConError)
    }

    @Test
    fun `onLoginClick con contrasena vacia marca campoConError CONTRASENA`() = runTest {
        vm.onUsuarioOCorreoChange("usuario@test.com")
        vm.onLoginClick()
        advanceUntilIdle()
        assertEquals(CampoLogin.CONTRASENA, vm.uiState.value.campoConError)
    }

    @Test
    fun `login exitoso guarda token y emite NavegarAHome`() = runTest {
        repo.encolarLogin(Resultado.Exito(LoginResponse(token = "jwt_falso_123")))
        val eventos = mutableListOf<LoginEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onUsuarioOCorreoChange("usuario@test.com")
        vm.onContrasenaChange("pass123")
        vm.onLoginClick()
        advanceUntilIdle()

        job.cancel()
        assertEquals("jwt_falso_123", sesion.tokenGuardado)
        assertTrue(eventos.any { it is LoginEvento.NavegarAHome })
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun `login INVALID_CREDENTIALS muestra error general`() = runTest {
        repo.encolarLogin(Resultado.Fallo(EraError.CredencialesInvalidas))

        vm.onUsuarioOCorreoChange("usuario@test.com")
        vm.onContrasenaChange("wrong")
        vm.onLoginClick()
        advanceUntilIdle()

        assertEquals(EraError.CredencialesInvalidas, vm.uiState.value.errorGeneral)
        assertFalse(vm.uiState.value.cargando)
    }

    @Test
    fun `login ACCOUNT_LOCKED muestra error de bloqueo`() = runTest {
        repo.encolarLogin(Resultado.Fallo(EraError.CuentaBloqueada))

        vm.onUsuarioOCorreoChange("usuario@test.com")
        vm.onContrasenaChange("pass123")
        vm.onLoginClick()
        advanceUntilIdle()

        assertEquals(EraError.CuentaBloqueada, vm.uiState.value.errorGeneral)
    }

    @Test
    fun `login ACCOUNT_INACTIVE limpia token y emite NavegarALogin`() = runTest {
        sesion.tokenGuardado = "token_previo"
        repo.encolarLogin(Resultado.Fallo(EraError.CuentaInactiva))
        val eventos = mutableListOf<LoginEvento>()
        val job = launch { vm.eventos.collect { eventos += it } }

        vm.onUsuarioOCorreoChange("usuario@test.com")
        vm.onContrasenaChange("pass123")
        vm.onLoginClick()
        advanceUntilIdle()

        job.cancel()
        assertTrue(sesion.fueLimpiado)
        assertTrue(eventos.any { it is LoginEvento.NavegarALogin })
    }

    @Test
    fun `login IOException muestra error de conexion`() = runTest {
        repo.encolarLogin(Resultado.Fallo(EraError.ErrorConexion))

        vm.onUsuarioOCorreoChange("usuario@test.com")
        vm.onContrasenaChange("pass123")
        vm.onLoginClick()
        advanceUntilIdle()

        assertEquals(EraError.ErrorConexion, vm.uiState.value.errorGeneral)
    }

    @Test
    fun `limpiar campo borra errorGeneral y campoConError`() = runTest {
        repo.encolarLogin(Resultado.Fallo(EraError.CredencialesInvalidas))

        vm.onUsuarioOCorreoChange("u@t.com")
        vm.onContrasenaChange("pass")
        vm.onLoginClick()
        advanceUntilIdle()

        assertEquals(EraError.CredencialesInvalidas, vm.uiState.value.errorGeneral)

        vm.onUsuarioOCorreoChange("nuevo@t.com")
        assertNull(vm.uiState.value.errorGeneral)
        assertNull(vm.uiState.value.campoConError)
    }

    @Test
    fun `toggle contrasenaVisible cambia el estado`() {
        assertFalse(vm.uiState.value.contrasenaVisible)
        vm.onContrasenaVisibleToggle()
        assertTrue(vm.uiState.value.contrasenaVisible)
        vm.onContrasenaVisibleToggle()
        assertFalse(vm.uiState.value.contrasenaVisible)
    }

    // ---------- Fakes ----------

    private class FakeAuthRepository : AuthRepository {
        val llamadas = mutableListOf<String>()
        private val loginQueue = ArrayDeque<Resultado<LoginResponse>>()

        fun encolarLogin(respuesta: Resultado<LoginResponse>) { loginQueue += respuesta }

        override suspend fun register(request: RegisterRequest): Resultado<Unit> =
            error("No usado en login tests")

        override suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit> =
            error("No usado en login tests")

        override suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit> =
            error("No usado en login tests")

        override suspend fun login(request: LoginRequest): Resultado<LoginResponse> {
            llamadas += "login"
            return loginQueue.removeFirstOrNull()
                ?: error("login llamado sin respuesta encolada")
        }

        override suspend fun logout(): Resultado<com.era.app.remote.dto.common.MessageResponse> =
            error("No usado en login tests")

        override suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit> =
            error("No usado en login tests")

        override suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse> =
            error("No usado en login tests")

        override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit> =
            error("No usado en login tests")
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
