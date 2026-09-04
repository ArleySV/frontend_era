package com.era.app.ui.home

import com.era.app.repository.AuthRepository
import com.era.app.repository.AvatarRepository
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.repository.UserRepository
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
import com.era.app.remote.dto.user.UserProfile
import com.era.app.utils.EraError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var user: FakeUserRepository
    private lateinit var sesion: FakeSesionRepository
    private lateinit var auth: FakeAuthRepository
    private lateinit var avatar: FakeAvatarRepository
    private lateinit var vm: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        user = FakeUserRepository()
        sesion = FakeSesionRepository()
        auth = FakeAuthRepository()
        avatar = FakeAvatarRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearVm(): HomeViewModel = HomeViewModel(auth, sesion, user, avatar)

    @Test
    fun `estado inicial con perfil cargando`() {
        vm = crearVm()
        assertTrue(vm.uiState.value.cargandoPerfil)
    }

    @Test
    fun `exito de perfil rellena saludo correo y avatar`() = runTest {
        user.resultado = Resultado.Exito(
            UserProfile(
                nombreMenor = "Sebastián",
                fechaNacimiento = "2016-05-01",
                correo = "acu@correo.com",
                nombreUsuario = "sebas",
                avatar = "preset:1",
            )
        )
        vm = crearVm()
        dispatcher.scheduler.advanceUntilIdle()
        val st = vm.uiState.value
        assertEquals("Sebastián", st.nombreMenor)
        assertEquals("acu@correo.com", st.correo)
        assertEquals("preset:1", st.avatar)
        assertFalse(st.cargandoPerfil)
    }

    @Test
    fun `exito de perfil persiste el correo como userId de sesion`() = runTest {
        user.resultado = Resultado.Exito(
            UserProfile(
                nombreMenor = "Sebastián",
                fechaNacimiento = "2016-05-01",
                correo = "x@y.com",
                nombreUsuario = "sebas",
                avatar = "preset:1",
            )
        )
        vm = crearVm()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("x@y.com", sesion.correoGuardado)
    }

    @Test
    fun `fallo de perfil usa fallback generico con correo de sesion`() = runTest {
        user.resultado = Resultado.Fallo(EraError.ErrorConexion)
        sesion.correoGuardado = "fallback@correo.com"
        vm = crearVm()
        dispatcher.scheduler.advanceUntilIdle()
        val st = vm.uiState.value
        assertEquals("", st.nombreMenor)
        assertEquals("fallback@correo.com", st.correo)
        assertFalse(st.cargandoPerfil)
    }

    @Test
    fun `avatar custom descarga bytes del binario`() = runTest {
        avatar.respuesta = Resultado.Exito(byteArrayOf(1, 2, 3))
        user.resultado = Resultado.Exito(
            UserProfile(
                nombreMenor = "Sebastián",
                fechaNacimiento = "2016-05-01",
                correo = "acu@correo.com",
                nombreUsuario = "sebas",
                avatar = "custom:9",
            )
        )
        vm = crearVm()
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(avatar.conteoDescargas == 1)
        assertTrue(vm.uiState.value.bytesAvatarCustom?.contentEquals(byteArrayOf(1, 2, 3)) == true)
    }

    @Test
    fun `avatar preset no descarga binario`() = runTest {
        user.resultado = Resultado.Exito(
            UserProfile(
                nombreMenor = "Sebastián",
                fechaNacimiento = "2016-05-01",
                correo = "acu@correo.com",
                nombreUsuario = "sebas",
                avatar = "preset:2",
            )
        )
        vm = crearVm()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, avatar.conteoDescargas)
        assertEquals(null, vm.uiState.value.bytesAvatarCustom)
    }

    @Test
    fun `clic en cerrar sesion abre el dialogo`() {
        vm = crearVm()
        vm.onCerrarSesionClick()
        assertTrue(vm.uiState.value.dialogoCierreVisible)
    }

    @Test
    fun `cancelar cierra el dialogo`() {
        vm = crearVm()
        vm.onCerrarSesionClick()
        vm.onCancelarCierre()
        assertFalse(vm.uiState.value.dialogoCierreVisible)
    }

    @Test
    fun `confirmar cierre limpia token y emite navegar a login`() = runTest {
        sesion.tokenGuardado = "abc"
        vm = crearVm()
        val eventos = Channel<HomeEvento>(Channel.BUFFERED)
        val job = launch { vm.eventos.collect { eventos.send(it) } }
        vm.onConfirmarCierre()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(HomeEvento.NavegarALogin, eventos.receive())
        assertTrue(sesion.fueLimpiado)
        assertTrue(auth.fueLlamado)
        assertFalse(vm.uiState.value.dialogoCierreVisible)
        assertFalse(vm.uiState.value.cerrando)
        job.cancel()
    }

    @Test
    fun `confirmar cierre es unico ante doble tap`() = runTest {
        sesion.tokenGuardado = "abc"
        vm = crearVm()
        val eventos = Channel<HomeEvento>(Channel.BUFFERED)
        val job = launch { vm.eventos.collect { eventos.send(it) } }
        vm.onConfirmarCierre()
        vm.onConfirmarCierre()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, auth.conteoLogout)
        job.cancel()
    }

    private class FakeUserRepository : UserRepository {
        var resultado: Resultado<UserProfile> = Resultado.Fallo(EraError.ErrorConexion)
        override suspend fun obtenerPerfil(): Resultado<UserProfile> = resultado
        override suspend fun actualizarNombreUsuario(nombre: String): Resultado<UserProfile> =
            Resultado.Fallo(EraError.ErrorConexion)
        override suspend fun eliminarCuenta(contrasena: String): Resultado<Unit> =
            Resultado.Fallo(EraError.ErrorConexion)
    }

    private class FakeSesionRepository : SesionRepository {
        var tokenGuardado: String? = null
        var correoGuardado: String? = null
        var fueLimpiado = false

        override fun guardarToken(token: String) { tokenGuardado = token }
        override fun obtenerToken(): String? = tokenGuardado
        override fun guardarCorreo(correo: String) { correoGuardado = correo }
        override fun obtenerCorreo(): String? = correoGuardado
        override fun limpiarToken() { tokenGuardado = null; fueLimpiado = true }
        override fun tieneToken(): Boolean = tokenGuardado != null
    }

    private class FakeAvatarRepository : AvatarRepository {
        var respuesta: Resultado<ByteArray> = Resultado.Fallo(EraError.ErrorConexion)
        var conteoDescargas = 0

        override suspend fun subirAvatar(
            bytes: ByteArray,
            filename: String?,
            mimeType: String,
        ): Resultado<Unit> = error("No usado en home tests")

        override suspend fun obtenerAvatarBytes(): Resultado<ByteArray> {
            conteoDescargas++
            return respuesta
        }
    }

    private class FakeAuthRepository : AuthRepository {
        var fueLlamado = false
        var conteoLogout = 0

        override suspend fun register(request: RegisterRequest): Resultado<Unit> =
            error("No usado en home tests")

        override suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit> =
            error("No usado en home tests")

        override suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit> =
            error("No usado en home tests")

        override suspend fun login(request: LoginRequest): Resultado<LoginResponse> =
            error("No usado en home tests")

        override suspend fun logout(): Resultado<MessageResponse> {
            fueLlamado = true
            conteoLogout++
            return Resultado.Exito(MessageResponse(message = "ok"))
        }

        override suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit> =
            error("No usado en home tests")

        override suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse> =
            error("No usado en home tests")

        override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit> =
            error("No usado en home tests")
    }
}
