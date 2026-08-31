package com.era.app.ui.progreso

import com.era.app.data.model.NivelConProgreso
import com.era.app.repository.ProgresoRepository
import com.era.app.repository.Resultado
import com.era.app.utils.EraError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
class ProgresoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeProgresoRepository
    private lateinit var vm: ProgresoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeProgresoRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial carga niveles del repositorio`() = runTest {
        val niveles = listOf(
            nivel(1, "COMPLETADO", 2),
            nivel(2, "DISPONIBLE", 0)
        )
        repo.nivelesFlow.value = niveles
        
        vm = ProgresoViewModel(repo)
        
        val eventos = mutableListOf<ProgresoUiState>()
        val job = launch { vm.uiState.collect { eventos.add(it) } }
        
        advanceUntilIdle()

        val state = eventos.last { it.niveles.isNotEmpty() }

        assertEquals(2, state.niveles.size)
        assertEquals(1, state.nivelesCompletados)
        assertEquals(0.05f, state.porcentaje)
        assertEquals(2, state.reintentosTotales)
        job.cancel()
    }

    @Test
    fun `sincronizar exito limpia error`() = runTest {
        repo.respuestaSync = Resultado.Exito(Unit)
        vm = ProgresoViewModel(repo)
        
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()
        
        vm.sincronizar()
        advanceUntilIdle()
        
        assertFalse(vm.uiState.value.sincronizando)
        assertNull(vm.uiState.value.error)
        job.cancel()
    }

    @Test
    fun `sincronizar fallo emite evento error`() = runTest {
        repo.respuestaSync = Resultado.Fallo(EraError.ErrorConexion)
        vm = ProgresoViewModel(repo)
        
        val jobUi = launch { vm.uiState.collect { } }
        val eventos = mutableListOf<ProgresoEvento>()
        val jobEventos = launch { vm.eventos.collect { eventos.add(it) } }
        
        advanceUntilIdle()

        vm.sincronizar()
        advanceUntilIdle()

        assertEquals(EraError.ErrorConexion, vm.uiState.value.error)
        assertTrue(eventos.any { it is ProgresoEvento.Error })
        
        jobUi.cancel()
        jobEventos.cancel()
    }

    @Test
    fun `confirmarReset exito emite ResetExitoso`() = runTest {
        repo.respuestaReset = Resultado.Exito(Unit)
        vm = ProgresoViewModel(repo)
        
        val jobUi = launch { vm.uiState.collect { } }
        val eventos = mutableListOf<ProgresoEvento>()
        val jobEventos = launch { vm.eventos.collect { eventos.add(it) } }
        
        advanceUntilIdle()

        vm.onReiniciarProgresoClick()
        vm.onContrasenaResetChange("pass")
        vm.onConfirmarReset()
        advanceUntilIdle()

        assertTrue(eventos.any { it is ProgresoEvento.ResetExitoso })
        assertFalse(vm.uiState.value.dialogoResetVisible)
        
        jobUi.cancel()
        jobEventos.cancel()
    }

    private fun nivel(orden: Int, estado: String, intentos: Int) = NivelConProgreso(
        orden = orden,
        pregunta = "P$orden",
        opcionA = "A", opcionB = "B", opcionC = "C",
        respuestaCorrecta = 0,
        estado = estado,
        intentosTotales = intentos,
        intentosFallidosConsecutivos = 0,
        completadoEn = null,
        sincronizado = true
    )

    private class FakeProgresoRepository : ProgresoRepository {
        val nivelesFlow = MutableStateFlow<List<NivelConProgreso>>(emptyList())
        var respuestaSync: Resultado<Unit> = Resultado.Exito(Unit)
        var respuestaReset: Resultado<Unit> = Resultado.Exito(Unit)

        override fun obtenerNivelesConProgreso(): Flow<List<NivelConProgreso>> = nivelesFlow
        override suspend fun registrarResultado(orden: Int, exito: Boolean): Resultado<Unit> = error("No usado")
        override suspend fun sincronizarConServidor(): Resultado<Unit> = respuestaSync
        override suspend fun reiniciarProgreso(contrasena: String): Resultado<Unit> = respuestaReset
    }
}
