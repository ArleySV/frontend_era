package com.era.app.ui.register

import com.era.app.repository.AuthRepository
import com.era.app.repository.Resultado
import com.era.app.remote.dto.auth.LoginRequest
import com.era.app.remote.dto.auth.LoginResponse
import com.era.app.remote.dto.auth.RegisterRequest
import com.era.app.remote.dto.auth.ResendOtpRequest
import com.era.app.remote.dto.auth.VerifyEmailRequest
import com.era.app.utils.EraError
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var vm: RegistroViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeAuthRepository()
        vm = RegistroViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fechaConEdad(anios: Int): String =
        LocalDate.now().minusYears(anios.toLong()).format(FECHA_DISPLAY)

    private fun llenarPaso1Valido() {
        vm.onNombreMenorChange("Ana")
        vm.onFechaNacimientoChange(fechaConEdad(9))
        vm.onNombreAcudienteChange("María Pérez")
        vm.onCedulaAcudienteChange("123456789")
    }

    private fun llenarPaso2Valido() {
        vm.onCorreoChange("acudiente@test.com")
        vm.onNombreUsuarioChange("ana_p")
        vm.onAvatarSeleccionar(1)
        vm.onContrasenaChange(CLAVE_VALIDA)
        vm.onConfirmarContrasenaChange(CLAVE_VALIDA)
    }

    // ---------- Paso 1 ----------

    @Test
    fun `paso 1 valido emite navegar a paso 2`() = runTest(dispatcher.scheduler) {
        llenarPaso1Valido()
        val eventos = mutableListOf<RegistroEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        vm.continuarPaso1()
        advanceUntilIdle()
        colector.cancel()

        assertEquals(listOf<RegistroEvento>(RegistroEvento.NavegarAPaso2), eventos)
        assertTrue(vm.uiState.value.errores.isEmpty())
    }

    @Test
    fun `paso 1 vacio marca los cuatro campos sin navegar`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RegistroEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        vm.continuarPaso1()
        advanceUntilIdle()
        colector.cancel()

        assertEquals(
            setOf(
                CampoRegistro.NOMBRE_MENOR,
                CampoRegistro.FECHA_NACIMIENTO,
                CampoRegistro.NOMBRE_ACUDIENTE,
                CampoRegistro.CEDULA_ACUDIENTE,
            ),
            vm.uiState.value.errores,
        )
        assertTrue(eventos.isEmpty())
    }

    @Test
    fun `cedula ux frontera quince pasa y dieciseis falla`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RegistroEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }
        vm.onNombreMenorChange("Ana")
        vm.onFechaNacimientoChange(fechaConEdad(9))
        vm.onNombreAcudienteChange("María Pérez")

        vm.onCedulaAcudienteChange("1".repeat(15))
        vm.continuarPaso1()
        advanceUntilIdle()
        assertEquals(1, eventos.size)

        vm.onCedulaAcudienteChange("1".repeat(16))
        vm.continuarPaso1()
        advanceUntilIdle()
        assertTrue(CampoRegistro.CEDULA_ACUDIENTE in vm.uiState.value.errores)
        assertEquals(1, eventos.size)
        colector.cancel()
    }

    @Test
    fun `cedula con letra guion o digitos unicode falla`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RegistroEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }
        vm.onNombreMenorChange("Ana")
        vm.onFechaNacimientoChange(fechaConEdad(9))
        vm.onNombreAcudienteChange("María Pérez")
        listOf("12345678901234a", "1234-567890123", "١٢٣٤٥٦٧٨٩٠١٢٣٤٥").forEach { cedula ->
            vm.onCedulaAcudienteChange(cedula)
            vm.continuarPaso1()
        }
        advanceUntilIdle()
        assertTrue(CampoRegistro.CEDULA_ACUDIENTE in vm.uiState.value.errores)
        assertTrue(eventos.isEmpty())
        colector.cancel()
    }

    @Test
    fun `edad frontera siete y once pasan seis y doce fallan`() = runTest(dispatcher.scheduler) {
        val eventos = mutableListOf<RegistroEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }
        vm.onNombreMenorChange("Ana")
        vm.onNombreAcudienteChange("María Pérez")
        vm.onCedulaAcudienteChange("123456789")

        listOf(7, 11).forEach { anios ->
            vm.onFechaNacimientoChange(fechaConEdad(anios))
            vm.continuarPaso1()
        }
        advanceUntilIdle()
        assertEquals(2, eventos.size)

        listOf(6, 12).forEach { anios ->
            vm.onFechaNacimientoChange(fechaConEdad(anios))
            vm.continuarPaso1()
        }
        advanceUntilIdle()
        assertEquals(2, eventos.size)
        assertTrue(CampoRegistro.FECHA_NACIMIENTO in vm.uiState.value.errores)
        colector.cancel()
    }

    @Test
    fun `fecha futura formato invalido o dia inexistente falla`() = runTest(dispatcher.scheduler) {
        val hoy = LocalDate.now()
        vm.onNombreMenorChange("Ana")
        vm.onNombreAcudienteChange("María Pérez")
        vm.onCedulaAcudienteChange("123456789")
        listOf(
            hoy.plusDays(1).format(FECHA_DISPLAY),
            "31/02/2016",
            "10-05-2016",
            "",
        ).forEach { fecha ->
            vm.onFechaNacimientoChange(fecha)
            vm.continuarPaso1()
        }
        advanceUntilIdle()
        assertTrue(CampoRegistro.FECHA_NACIMIENTO in vm.uiState.value.errores)
    }

    @Test
    fun `editar un campo limpia solo su error`() = runTest(dispatcher.scheduler) {
        vm.continuarPaso1()
        advanceUntilIdle()
        assertEquals(4, vm.uiState.value.errores.size)

        vm.onNombreMenorChange("Ana")
        assertEquals(
            3,
            vm.uiState.value.errores.size,
        )
        assertFalse(CampoRegistro.NOMBRE_MENOR in vm.uiState.value.errores)
    }

    @Test
    fun `cancelar restaura el estado inicial`() = runTest(dispatcher.scheduler) {
        llenarPaso1Valido()
        vm.continuarPaso1()
        llenarPaso2Valido()
        vm.cancelar()
        advanceUntilIdle()
        assertEquals(RegistroUiState(), vm.uiState.value)
    }

    // ---------- Paso 2 ----------

    @Test
    fun `paso 2 valido envia request correcto y navega a paso 3`() =
        runTest(dispatcher.scheduler) {
            repo.encolarRegister(Resultado.Exito(Unit))
            val eventos = mutableListOf<RegistroEvento>()
            val colector = launch { vm.eventos.collect { eventos += it } }

            llenarPaso1Valido()
            vm.continuarPaso1()
            vm.onNombreMenorChange("  Ana  ")
            llenarPaso2Valido()
            vm.onCorreoChange(" acudiente@test.com ")
            vm.continuarPaso2()
            runCurrent()
            colector.cancel()

            val req = repo.registerRequests.single()
            assertEquals("Ana", req.nombreMenor)
            assertEquals(LocalDate.now().minusYears(9).toString(), req.fechaNacimiento)
            assertEquals("María Pérez", req.nombreAcudiente)
            assertEquals("123456789", req.cedulaAcudiente)
            assertEquals("acudiente@test.com", req.correo)
            assertEquals("ana_p", req.nombreUsuario)
            assertEquals("preset:1", req.avatar)
            assertEquals(CLAVE_VALIDA, req.contrasena)
            assertEquals(CLAVE_VALIDA, req.confirmarContrasena)

            assertTrue(RegistroEvento.NavegarAPaso3 in eventos)
            assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)
        }

    @Test
    fun `contrasenas distintas cortocircuita sin llamar al repositorio`() =
        runTest(dispatcher.scheduler) {
            llenarPaso1Valido()
            vm.continuarPaso1()
            llenarPaso2Valido()
            vm.onConfirmarContrasenaChange("OtraClave1!")

            vm.continuarPaso2()
            advanceUntilIdle()

            assertTrue(CampoRegistro.CONFIRMAR_CONTRASENA in vm.uiState.value.errores)
            assertTrue(repo.llamadas.isEmpty())
        }

    @Test
    fun `politica incompleta marca contrasena expone criterios y no llama repositorio`() =
        runTest(dispatcher.scheduler) {
            llenarPaso1Valido()
            vm.continuarPaso1()
            vm.onCorreoChange("acudiente@test.com")
            vm.onNombreUsuarioChange("ana_p")
            vm.onAvatarSeleccionar(2)
            vm.onContrasenaChange("abc")
            vm.onConfirmarContrasenaChange("abc")

            vm.continuarPaso2()
            advanceUntilIdle()

            assertTrue(CampoRegistro.CONTRASENA in vm.uiState.value.errores)
            assertTrue(repo.llamadas.isEmpty())
            with(vm.uiState.value.criteriosContrasena) {
                assertFalse(longitudMinima)
                assertFalse(tieneMayuscula)
                assertTrue(tieneMinuscula)
                assertFalse(tieneNumero)
                assertFalse(tieneSimbolo)
            }
        }

    @Test
    fun `avatar sin seleccionar impide avanzar`() = runTest(dispatcher.scheduler) {
        llenarPaso1Valido()
        vm.continuarPaso1()
        vm.onCorreoChange("acudiente@test.com")
        vm.onNombreUsuarioChange("ana_p")
        vm.onContrasenaChange(CLAVE_VALIDA)
        vm.onConfirmarContrasenaChange(CLAVE_VALIDA)

        vm.continuarPaso2()
        advanceUntilIdle()

        assertTrue(CampoRegistro.AVATAR in vm.uiState.value.errores)
        assertTrue(repo.llamadas.isEmpty())
    }

    @Test
    fun `correo registrado muestra error inline y permanece en paso 2`() =
        runTest(dispatcher.scheduler) {
            repo.encolarRegister(Resultado.Fallo(EraError.CorreoRegistrado))
            llenarPaso1Valido()
            vm.continuarPaso1()
            llenarPaso2Valido()

            vm.continuarPaso2()
            advanceUntilIdle()

            assertTrue(CampoRegistro.CORREO in vm.uiState.value.errores)
            assertEquals(EraError.CorreoRegistrado, vm.uiState.value.errorGeneral)
            assertEquals(listOf("register"), repo.llamadas)
        }

    @Test
    fun `usuario en uso marca el campo username`() = runTest(dispatcher.scheduler) {
        repo.encolarRegister(Resultado.Fallo(EraError.UsuarioEnUso))
        llenarPaso1Valido()
        vm.continuarPaso1()
        llenarPaso2Valido()

        vm.continuarPaso2()
        advanceUntilIdle()

        assertTrue(CampoRegistro.NOMBRE_USUARIO in vm.uiState.value.errores)
        assertEquals(EraError.UsuarioEnUso, vm.uiState.value.errorGeneral)
    }

    @Test
    fun `error de conexion emite aviso y conserva los datos`() = runTest(dispatcher.scheduler) {
        repo.encolarRegister(Resultado.Fallo(EraError.ErrorConexion))
        llenarPaso1Valido()
        vm.continuarPaso1()
        llenarPaso2Valido()
        val eventos = mutableListOf<RegistroEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        vm.continuarPaso2()
        advanceUntilIdle()
        colector.cancel()

        val aviso = eventos.filterIsInstance<RegistroEvento.Aviso>().single()
        assertEquals(EraError.ErrorConexion, aviso.error)
        assertEquals("acudiente@test.com", vm.uiState.value.correo)
        assertEquals("ana_p", vm.uiState.value.nombreUsuario)
        assertEquals(CLAVE_VALIDA, vm.uiState.value.contrasena)
        assertFalse(RegistroEvento.NavegarAPaso3 in eventos)
    }

    @Test
    fun `validacion del servidor va a error general`() = runTest(dispatcher.scheduler) {
        repo.encolarRegister(
            Resultado.Fallo(
                EraError.Validacion(listOf("La contrasena no cumple la politica"))
            )
        )
        llenarPaso1Valido()
        vm.continuarPaso1()
        llenarPaso2Valido()

        vm.continuarPaso2()
        advanceUntilIdle()

        assertEquals(
            EraError.Validacion(listOf("La contrasena no cumple la politica")),
            vm.uiState.value.errorGeneral,
        )
    }

    // ---------- Checklist de contraseña ----------

    @Test
    fun `el checklist se actualiza al teclear contrasena y username`() =
        runTest(dispatcher.scheduler) {
            vm.onContrasenaChange("abc")
            with(vm.uiState.value.criteriosContrasena) {
                assertFalse(longitudMinima)
                assertFalse(tieneMayuscula)
                assertTrue(tieneMinuscula)
                assertFalse(tieneNumero)
                assertFalse(tieneSimbolo)
            }

            vm.onNombreUsuarioChange("ABC")
            assertFalse(vm.uiState.value.criteriosContrasena.distintaDeUsuario)

            vm.onContrasenaChange(CLAVE_VALIDA)
            with(vm.uiState.value.criteriosContrasena) {
                assertTrue(longitudMinima)
                assertTrue(tieneMayuscula)
                assertTrue(tieneMinuscula)
                assertTrue(tieneNumero)
                assertTrue(tieneSimbolo)
                assertTrue(distintaDeUsuario)
            }
        }

    // ---------- Paso 3 ----------

    @Test
    fun `otp localmente invalido no llama al repositorio`() = runTest(dispatcher.scheduler) {
        vm.onCodigoOtpChange("12a456")

        assertEquals("12456", vm.uiState.value.codigoOtp)
        vm.verificarCodigo()
        advanceUntilIdle()

        assertTrue(CampoRegistro.CODIGO_OTP in vm.uiState.value.errores)
        assertTrue(repo.llamadas.isEmpty())
    }

    @Test
    fun `verificacion exitosa emite ir a login`() = runTest(dispatcher.scheduler) {
        repo.encolarRegister(Resultado.Exito(Unit))
        repo.encolarVerify(Resultado.Exito(Unit))
        llenarPaso1Valido()
        vm.continuarPaso1()
        llenarPaso2Valido()
        vm.continuarPaso2()
        advanceUntilIdle()
        vm.onCodigoOtpChange("123456")
        val eventos = mutableListOf<RegistroEvento>()
        val colector = launch { vm.eventos.collect { eventos += it } }

        vm.verificarCodigo()
        advanceUntilIdle()
        colector.cancel()

        assertTrue(RegistroEvento.RegistroVerificadoIrALogin in eventos)
        assertTrue(repo.llamadas.contains("verifyEmail"))
    }

    @Test
    fun `otp invalido del servidor marca el campo`() = runTest(dispatcher.scheduler) {
        repo.encolarVerify(Resultado.Fallo(EraError.OtpInvalido))
        vm.onCodigoOtpChange("000000")

        vm.verificarCodigo()
        advanceUntilIdle()

        assertTrue(CampoRegistro.CODIGO_OTP in vm.uiState.value.errores)
        assertEquals(EraError.OtpInvalido, vm.uiState.value.errorGeneral)
    }

    // ---------- Countdown de reenvío (D-10) ----------

    @Test
    fun `reenvio bloqueado durante el countdown no llama al repositorio`() =
        runTest(dispatcher.scheduler) {
            repo.encolarRegister(Resultado.Exito(Unit))
            llenarPaso1Valido()
            vm.continuarPaso1()
            llenarPaso2Valido()
            vm.continuarPaso2()
            runCurrent()

            vm.reenviarCodigo()
            advanceUntilIdle()

            assertEquals(listOf("register"), repo.llamadas)
        }

    @Test
    fun `contador habilita reenvio tras sesenta segundos virtuales`() =
        runTest(dispatcher.scheduler) {
            repo.encolarRegister(Resultado.Exito(Unit))
            repo.encolarResend(Resultado.Exito(Unit))
            llenarPaso1Valido()
            vm.continuarPaso1()
            llenarPaso2Valido()
            vm.continuarPaso2()
            runCurrent()
            assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)

            advanceTimeBy(59_000)
            runCurrent()
            assertEquals(1, vm.uiState.value.reenvioSegundosRestantes)
            vm.reenviarCodigo()
            runCurrent()
            assertEquals(listOf("register"), repo.llamadas)

            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(0, vm.uiState.value.reenvioSegundosRestantes)
            vm.reenviarCodigo()
            runCurrent()
            assertEquals(listOf("register", "resendOtp"), repo.llamadas)
            assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)
        }

    @Test
    fun `reenvio exitoso reinicia contador a sesenta`() = runTest(dispatcher.scheduler) {
        repo.encolarRegister(Resultado.Exito(Unit))
        repo.encolarResend(Resultado.Exito(Unit))
        llenarPaso1Valido()
        vm.continuarPaso1()
        llenarPaso2Valido()
        vm.continuarPaso2()
        advanceUntilIdle()
        vm.reenviarCodigo()
        runCurrent()

        assertEquals(60, vm.uiState.value.reenvioSegundosRestantes)
        assertEquals(listOf("register", "resendOtp"), repo.llamadas)
    }

    @Test
    fun `throttled del servidor emite aviso sin reiniciar contador`() =
        runTest(dispatcher.scheduler) {
            repo.encolarRegister(Resultado.Exito(Unit))
            repo.encolarResend(Resultado.Fallo(EraError.ReenvioThrottled))
            llenarPaso1Valido()
            vm.continuarPaso1()
            llenarPaso2Valido()
            vm.continuarPaso2()
            advanceUntilIdle()
            val eventos = mutableListOf<RegistroEvento>()
            val colector = launch { vm.eventos.collect { eventos += it } }

            vm.reenviarCodigo()
            advanceUntilIdle()
            colector.cancel()

            val aviso = eventos.filterIsInstance<RegistroEvento.Aviso>().single()
            assertEquals(EraError.ReenvioThrottled, aviso.error)
            assertEquals(0, vm.uiState.value.reenvioSegundosRestantes)
        }

    private companion object {
        const val CLAVE_VALIDA = "ClaveSegura1!"
        val FECHA_DISPLAY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    private class FakeAuthRepository : AuthRepository {

        val llamadas = mutableListOf<String>()
        val registerRequests = mutableListOf<RegisterRequest>()

        private val registerQueue = ArrayDeque<Resultado<Unit>>()
        private val verifyQueue = ArrayDeque<Resultado<Unit>>()
        private val resendQueue = ArrayDeque<Resultado<Unit>>()

        fun encolarRegister(respuesta: Resultado<Unit>) {
            registerQueue += respuesta
        }

        fun encolarVerify(respuesta: Resultado<Unit>) {
            verifyQueue += respuesta
        }

        fun encolarResend(respuesta: Resultado<Unit>) {
            resendQueue += respuesta
        }

        override suspend fun register(request: RegisterRequest): Resultado<Unit> {
            llamadas += "register"
            registerRequests += request
            return registerQueue.removeFirstOrNull()
                ?: error("register llamado sin respuesta encolada")
        }

        override suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit> {
            llamadas += "verifyEmail"
            return verifyQueue.removeFirstOrNull()
                ?: error("verifyEmail llamado sin respuesta encolada")
        }

        override suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit> {
            llamadas += "resendOtp"
            return resendQueue.removeFirstOrNull()
                ?: error("resendOtp llamado sin respuesta encolada")
        }

        override suspend fun login(request: LoginRequest): Resultado<LoginResponse> =
            error("No usado en registro tests")

        override suspend fun logout(): Resultado<com.era.app.remote.dto.common.MessageResponse> =
            error("No usado en registro tests")
    }
}
