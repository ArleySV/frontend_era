package com.era.app.ui.recuperacion

import com.era.app.repository.AuthRepository
import com.era.app.repository.Resultado
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
import com.era.app.utils.EraError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecuperacionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var vm: RecuperacionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        vm = RecuperacionViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun llenarCorreo() {
        vm.onCorreoChange("padre@test.com")
    }

    private fun TestScope.verificarConExito(token: String = "jwt-puente") {
        llenarCorreo()
        vm.onCodigoOtpChange("123456")
        repo.encolarVerifyPasswordReset(Resultado.Exito(PasswordResetVerifyResponse(resetToken = token)))
        vm.verificarCodigo()
        runCurrent()
    }

    // ---------- Estados ----------

    @Test
    fun `estado inicial tiene campos vacios countdown cero sin errores`() {
        val s = vm.uiState.value
        assertEquals("", s.correo)
        assertEquals("", s.codigoOtp)
        assertEquals("", s.nuevaContrasena)
        assertEquals("", s.confirmarContrasena)
        assertFalse(s.nuevaContrasenaVisible)
        assertFalse(s.confirmarVisible)
        assertEquals(0, s.reenvioSegundosRestantes)
        assertTrue(s.errores.isEmpty())
        assertNull(s.errorGeneral)
    }

    // ---------- Paso 1: enviarEnlace ----------

    @Test
    fun `enviarEnlace con correo invalido marca campo y no llama al repo`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        vm.onCorreoChange("correo-malo")
        vm.enviarEnlace()
        advanceUntilIdle()
        colector.cancel()

        assertTrue(CampoRecuperacion.CORREO in vm.uiState.value.errores)
        assertTrue(eventos.isEmpty())
        assertTrue(repo.llamadas.isEmpty())
    }

    @Test
    fun `enviarEnlace exitoso inicia countdown y navega a paso 2`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        repo.encolarRequestPasswordReset(Resultado.Exito(Unit))
        llenarCorreo()
        vm.enviarEnlace()
        runCurrent()

        assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, vm.uiState.value.reenvioSegundosRestantes)

        colector.cancel()
        assertEquals(listOf(RecuperacionEvento.NavegarAPaso2), eventos)
        assertEquals(listOf("requestPasswordReset"), repo.llamadas)
        assertEquals("padre@test.com", repo.requestPasswordResetRequests.single().correo)
    }

    @Test
    fun `enviarEnlace throttled muestra errorGeneral sin navegar`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        repo.encolarRequestPasswordReset(Resultado.Fallo(EraError.ReenvioThrottled))
        llenarCorreo()
        vm.enviarEnlace()
        advanceUntilIdle()
        colector.cancel()

        assertEquals(EraError.ReenvioThrottled, vm.uiState.value.errorGeneral)
        assertTrue(eventos.isEmpty())
    }

    @Test
    fun `enviarEnlace con validacion del servidor muestra detalles como errorGeneral`() = runTest(dispatcher.scheduler) {
        repo.encolarRequestPasswordReset(Resultado.Fallo(EraError.Validacion(listOf("Correo inválido."))))
        llenarCorreo()
        vm.enviarEnlace()
        advanceUntilIdle()

        assertEquals(EraError.Validacion(listOf("Correo inválido.")), vm.uiState.value.errorGeneral)
        assertTrue(vm.uiState.value.errores.isEmpty())
        assertTrue(repo.llamadas.isNotEmpty())
    }

    // ---------- Paso 2: OTP ----------

    @Test
    fun `onCodigoOtpChange filtra no digitos y limita a seis`() {
        vm.onCodigoOtpChange("12a3b456789")
        assertEquals("123456", vm.uiState.value.codigoOtp)
    }

    @Test
    fun `verificarCodigo con otp incompleto marca campo y no llama al repo`() = runTest(dispatcher.scheduler) {
        llenarCorreo()
        vm.onCodigoOtpChange("123")
        vm.verificarCodigo()
        advanceUntilIdle()

        assertTrue(CampoRecuperacion.CODIGO_OTP in vm.uiState.value.errores)
        assertTrue(repo.llamadas.isEmpty())
    }

    @Test
    fun `verificarCodigo exitoso guarda resetToken y navega a paso 3`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        repo.encolarVerifyPasswordReset(Resultado.Exito(PasswordResetVerifyResponse(resetToken = "jwt-puente")))
        llenarCorreo()
        vm.onCodigoOtpChange("123456")
        vm.verificarCodigo()
        advanceUntilIdle()
        colector.cancel()

        assertEquals(listOf(RecuperacionEvento.NavegarAPaso3), eventos)
        assertEquals(listOf("verifyPasswordReset"), repo.llamadas)
        assertEquals("padre@test.com", repo.verifyPasswordResetRequests.single().correo)
        assertEquals("123456", repo.verifyPasswordResetRequests.single().codigo)
    }

    @Test
    fun `verificarCodigo con otp invalido del servidor muestra errorGeneral sin navegar`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        repo.encolarVerifyPasswordReset(Resultado.Fallo(EraError.OtpInvalido))
        llenarCorreo()
        vm.onCodigoOtpChange("999999")
        vm.verificarCodigo()
        advanceUntilIdle()
        colector.cancel()

        assertEquals(EraError.OtpInvalido, vm.uiState.value.errorGeneral)
        assertTrue(CampoRecuperacion.CODIGO_OTP !in vm.uiState.value.errores)
        assertTrue(eventos.isEmpty())
    }

    @Test
    fun `verificarCodigo sin correo reinicia flujo sin llamar al repo`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        vm.onCodigoOtpChange("123456")
        vm.verificarCodigo()
        advanceUntilIdle()
        colector.cancel()

        assertTrue(RecuperacionEvento.ReiniciarFlujo in eventos)
        assertTrue(repo.llamadas.isEmpty())
    }

    // ---------- Paso 2: reenviarCodigo ----------

    @Test
    fun `reenviarCodigo durante countdown no llama al repo`() = runTest(dispatcher.scheduler) {
        repo.encolarRequestPasswordReset(Resultado.Exito(Unit))
        llenarCorreo()
        vm.enviarEnlace()
        runCurrent()
        assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)

        vm.reenviarCodigo()
        advanceUntilIdle()

        assertEquals(listOf("requestPasswordReset"), repo.llamadas)
    }

    @Test
    fun `reenviarCodigo exitoso reinicia countdown a sesenta`() = runTest(dispatcher.scheduler) {
        repo.encolarRequestPasswordReset(Resultado.Exito(Unit))
        llenarCorreo()
        vm.reenviarCodigo()
        runCurrent()

        assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)
        assertEquals(listOf("requestPasswordReset"), repo.llamadas)
    }

    @Test
    fun `reenviarCodigo throttled emite aviso sin errorGeneral`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        repo.encolarRequestPasswordReset(Resultado.Fallo(EraError.ReenvioThrottled))
        llenarCorreo()
        vm.reenviarCodigo()
        advanceUntilIdle()
        colector.cancel()

        val aviso = eventos.filterIsInstance<RecuperacionEvento.Aviso>().single()
        assertEquals(EraError.ReenvioThrottled, aviso.error)
        assertNull(vm.uiState.value.errorGeneral)
    }

    // ---------- Paso 3: guardarContrasena ----------

    @Test
    fun `guardarContrasena con politica invalida marca campo y no llama al repo`() = runTest(dispatcher.scheduler) {
        verificarConExito()
        vm.onNuevaContrasenaChange("corta")
        vm.onConfirmarContrasenaChange("corta")
        vm.guardarContrasena()
        advanceUntilIdle()

        assertTrue(CampoRecuperacion.NUEVA_CONTRASENA in vm.uiState.value.errores)
        assertFalse(repo.llamadas.contains("confirmPasswordReset"))
    }

    @Test
    fun `guardarContrasena con confirmacion distinta marca campo y no llama al repo`() = runTest(dispatcher.scheduler) {
        verificarConExito()
        vm.onNuevaContrasenaChange("NuevaClave1!")
        vm.onConfirmarContrasenaChange("OtraClave1!")
        vm.guardarContrasena()
        advanceUntilIdle()

        assertTrue(CampoRecuperacion.CONFIRMAR_CONTRASENA in vm.uiState.value.errores)
        assertFalse(repo.llamadas.contains("confirmPasswordReset"))
    }

    @Test
    fun `guardarContrasena exitosa limpia resetToken y emite exito`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        verificarConExito()
        repo.encolarConfirmPasswordReset(Resultado.Exito(Unit))
        vm.onNuevaContrasenaChange("NuevaClave1!")
        vm.onConfirmarContrasenaChange("NuevaClave1!")
        vm.guardarContrasena()
        advanceUntilIdle()

        assertTrue(RecuperacionEvento.RecuperacionExitosa in eventos)

        vm.guardarContrasena()
        advanceUntilIdle()
        colector.cancel()

        assertEquals("jwt-puente", repo.confirmPasswordResetRequests.single().resetToken)
        assertEquals(listOf("verifyPasswordReset", "confirmPasswordReset"), repo.llamadas)
    }

    @Test
    fun `guardarContrasena con password reusada muestra errorGeneral conservando campos`() = runTest(dispatcher.scheduler) {
        verificarConExito()
        repo.encolarConfirmPasswordReset(Resultado.Fallo(EraError.PasswordReusada))
        vm.onNuevaContrasenaChange("NuevaClave1!")
        vm.onConfirmarContrasenaChange("NuevaClave1!")
        vm.guardarContrasena()
        advanceUntilIdle()

        assertEquals(EraError.PasswordReusada, vm.uiState.value.errorGeneral)
        assertEquals("NuevaClave1!", vm.uiState.value.nuevaContrasena)
        assertEquals("NuevaClave1!", vm.uiState.value.confirmarContrasena)
        assertTrue(CampoRecuperacion.NUEVA_CONTRASENA !in vm.uiState.value.errores)
        assertTrue(CampoRecuperacion.CONFIRMAR_CONTRASENA !in vm.uiState.value.errores)
    }

    @Test
    fun `guardarContrasena con token invalido limpia token reinicia flujo conservando correo`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        verificarConExito()
        repo.encolarConfirmPasswordReset(Resultado.Fallo(EraError.ResetTokenInvalido))
        vm.onNuevaContrasenaChange("NuevaClave1!")
        vm.onConfirmarContrasenaChange("NuevaClave1!")
        vm.guardarContrasena()
        advanceUntilIdle()

        assertTrue(RecuperacionEvento.ReiniciarFlujo in eventos)
        assertEquals("padre@test.com", vm.uiState.value.correo)
        assertEquals("", vm.uiState.value.codigoOtp)
        assertEquals("", vm.uiState.value.nuevaContrasena)
        assertEquals(EraError.ResetTokenInvalido, vm.uiState.value.errorGeneral)

        vm.onNuevaContrasenaChange("NuevaClave2!")
        vm.onConfirmarContrasenaChange("NuevaClave2!")
        vm.guardarContrasena()
        advanceUntilIdle()
        colector.cancel()

        assertEquals(listOf("verifyPasswordReset", "confirmPasswordReset"), repo.llamadas)
    }

    // ---------- Guard de restauración y cancelar ----------

    @Test
    fun `guardarContrasena sin resetToken reinicia flujo sin llamar al repo`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RecuperacionEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        vm.onCorreoChange("padre@test.com")
        vm.onNuevaContrasenaChange("NuevaClave1!")
        vm.onConfirmarContrasenaChange("NuevaClave1!")
        vm.guardarContrasena()
        advanceUntilIdle()
        colector.cancel()

        assertTrue(RecuperacionEvento.ReiniciarFlujo in eventos)
        assertTrue(repo.llamadas.isEmpty())
        assertEquals("padre@test.com", vm.uiState.value.correo)
        assertEquals("NuevaClave1!", vm.uiState.value.nuevaContrasena)
    }

    @Test
    fun `cancelar cancela countdown y reinicia estado`() = runTest(dispatcher.scheduler) {
        repo.encolarRequestPasswordReset(Resultado.Exito(Unit))
        llenarCorreo()
        vm.enviarEnlace()
        runCurrent()
        assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)

        vm.cancelar()

        assertEquals(RecuperacionUiState(), vm.uiState.value)
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(0, vm.uiState.value.reenvioSegundosRestantes)
    }

    // ---------- Fake ----------

    private class FakeAuthRepository : AuthRepository {

        val llamadas = mutableListOf<String>()
        val requestPasswordResetRequests = mutableListOf<PasswordResetRequest>()
        val verifyPasswordResetRequests = mutableListOf<PasswordResetVerifyRequest>()
        val confirmPasswordResetRequests = mutableListOf<PasswordResetConfirmRequest>()

        private val requestQueue = ArrayDeque<Resultado<Unit>>()
        private val verifyQueue = ArrayDeque<Resultado<PasswordResetVerifyResponse>>()
        private val confirmQueue = ArrayDeque<Resultado<Unit>>()

        fun encolarRequestPasswordReset(respuesta: Resultado<Unit>) {
            requestQueue += respuesta
        }

        fun encolarVerifyPasswordReset(respuesta: Resultado<PasswordResetVerifyResponse>) {
            verifyQueue += respuesta
        }

        fun encolarConfirmPasswordReset(respuesta: Resultado<Unit>) {
            confirmQueue += respuesta
        }

        override suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit> {
            llamadas += "requestPasswordReset"
            requestPasswordResetRequests += request
            return requestQueue.removeFirstOrNull()
                ?: error("requestPasswordReset llamado sin respuesta encolada")
        }

        override suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse> {
            llamadas += "verifyPasswordReset"
            verifyPasswordResetRequests += request
            return verifyQueue.removeFirstOrNull()
                ?: error("verifyPasswordReset llamado sin respuesta encolada")
        }

        override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit> {
            llamadas += "confirmPasswordReset"
            confirmPasswordResetRequests += request
            return confirmQueue.removeFirstOrNull()
                ?: error("confirmPasswordReset llamado sin respuesta encolada")
        }

        override suspend fun register(request: RegisterRequest): Resultado<Unit> =
            error("No usado en recuperacion tests")

        override suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit> =
            error("No usado en recuperacion tests")

        override suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit> =
            error("No usado en recuperacion tests")

        override suspend fun login(request: LoginRequest): Resultado<LoginResponse> =
            error("No usado en recuperacion tests")

        override suspend fun logout(): Resultado<MessageResponse> =
            error("No usado en recuperacion tests")
    }
}