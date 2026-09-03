package com.era.app.ui.splash

import com.era.app.repository.SesionRepository
import com.era.app.ui.navigation.EraRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class SplashViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var sesion: FakeSesionRepository
    private lateinit var vm: SplashViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        sesion = FakeSesionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearVm(): SplashViewModel = SplashViewModel(sesion)

    @Test
    fun `estado inicial con cargando en verde`() {
        vm = crearVm()
        assertTrue(vm.uiState.value.cargando)
    }

    @Test
    fun `sin token emite NavegarALogin y detiene carga`() = runTest {
        vm = crearVm()
        val eventos = Channel<SplashEvento>(Channel.BUFFERED)
        val job = launch { vm.eventos.collect { eventos.send(it) } }

        advanceUntilIdle()

        val evento = eventos.receive()
        assertEquals(SplashEvento.NavegarALogin, evento)
        assertFalse(vm.uiState.value.cargando)
        job.cancel()
    }

    @Test
    fun `con token emite NavegarAHome hacia HOME_PLACEHOLDER y detiene carga`() = runTest {
        sesion.tokenGuardado = "jwt-test"
        vm = crearVm()
        val eventos = Channel<SplashEvento>(Channel.BUFFERED)
        val job = launch { vm.eventos.collect { eventos.send(it) } }

        advanceUntilIdle()

        val evento = eventos.receive()
        assertEquals(SplashEvento.NavegarAHome(EraRoutes.HOME_PLACEHOLDER), evento)
        assertFalse(vm.uiState.value.cargando)
        job.cancel()
    }

    @Test
    fun `sin token no navega a home`() = runTest {
        vm = crearVm()
        val eventos = Channel<SplashEvento>(Channel.BUFFERED)
        val job = launch { vm.eventos.collect { eventos.send(it) } }

        advanceUntilIdle()

        val evento = eventos.receive()
        assertTrue(evento is SplashEvento.NavegarALogin)
        job.cancel()
    }

    @Test
    fun `con token no navega a login`() = runTest {
        sesion.tokenGuardado = "jwt-test"
        vm = crearVm()
        val eventos = Channel<SplashEvento>(Channel.BUFFERED)
        val job = launch { vm.eventos.collect { eventos.send(it) } }

        advanceUntilIdle()

        val evento = eventos.receive()
        assertTrue(evento is SplashEvento.NavegarAHome)
        job.cancel()
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
