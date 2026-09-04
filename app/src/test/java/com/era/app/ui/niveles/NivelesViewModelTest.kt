package com.era.app.ui.niveles

import com.era.app.data.model.NivelConProgreso
import com.era.app.repository.ProgresoRepository
import com.era.app.repository.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class NivelesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeProgresoRepository
    private lateinit var vm: NivelesViewModel

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
    fun `estado inicial esta cargando y sin niveles`() = runTest {
        vm = NivelesViewModel(repo)

        assertTrue(vm.uiState.value.cargando)
        assertTrue(vm.uiState.value.niveles.isEmpty())
    }

    @Test
    fun `expone niveles del repositorio y deja de cargar`() = runTest {
        repo.nivelesFlow.value = listOf(
            nivel(1, "COMPLETADO"),
            nivel(2, "DISPONIBLE"),
            nivel(3, "BLOQUEADO"),
        )
        vm = NivelesViewModel(repo)

        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()

        assertFalse(vm.uiState.value.cargando)
        assertEquals(3, vm.uiState.value.niveles.size)
        assertEquals("DISPONIBLE", vm.uiState.value.niveles[1].estado)
        job.cancel()
    }

    @Test
    fun `refleja actualizaciones del flow del repositorio`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(1, "DISPONIBLE"))
        vm = NivelesViewModel(repo)

        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()
        assertEquals("DISPONIBLE", vm.uiState.value.niveles[0].estado)

        repo.nivelesFlow.value = listOf(nivel(1, "COMPLETADO"), nivel(2, "DISPONIBLE"))
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.niveles.size)
        assertEquals("COMPLETADO", vm.uiState.value.niveles[0].estado)
        job.cancel()
    }

    @Test
    fun `onNivelClick en disponible y completado emite NavegarAJuego con el orden`() = runTest {
        repo.nivelesFlow.value = listOf(
            nivel(1, "COMPLETADO"),
            nivel(2, "DISPONIBLE"),
        )
        vm = NivelesViewModel(repo)

        val jobUi = launch { vm.uiState.collect { } }
        val eventos = mutableListOf<NivelesEvento>()
        val jobEventos = launch { vm.eventos.collect { eventos.add(it) } }
        advanceUntilIdle()

        vm.onNivelClick(2)
        vm.onNivelClick(1)
        advanceUntilIdle()

        val ordenes = eventos.filterIsInstance<NivelesEvento.NavegarAJuego>().map { it.orden }
        assertEquals(listOf(2, 1), ordenes)

        jobUi.cancel()
        jobEventos.cancel()
    }

    @Test
    fun `onNivelClick en bloqueado no emite evento`() = runTest {
        repo.nivelesFlow.value = listOf(
            nivel(1, "DISPONIBLE"),
            nivel(2, "BLOQUEADO"),
        )
        vm = NivelesViewModel(repo)

        val jobUi = launch { vm.uiState.collect { } }
        val eventos = mutableListOf<NivelesEvento>()
        val jobEventos = launch { vm.eventos.collect { eventos.add(it) } }
        advanceUntilIdle()

        vm.onNivelClick(2)
        advanceUntilIdle()

        assertTrue(eventos.isEmpty())

        jobUi.cancel()
        jobEventos.cancel()
    }

    private fun nivel(orden: Int, estado: String) = NivelConProgreso(
        orden = orden,
        pregunta = "P$orden",
        opcionA = "A", opcionB = "B", opcionC = "C",
        respuestaCorrecta = 0,
        estado = estado,
        intentosTotales = 0,
        intentosFallidosConsecutivos = 0,
        completadoEn = null,
        sincronizado = true,
    )

    private class FakeProgresoRepository : ProgresoRepository {
        val nivelesFlow = MutableStateFlow<List<NivelConProgreso>>(emptyList())

        override fun obtenerNivelesConProgreso(): Flow<List<NivelConProgreso>> = nivelesFlow
        override suspend fun registrarResultado(orden: Int, exito: Boolean): Resultado<Unit> = error("No usado")
        override suspend fun sincronizarConServidor(): Resultado<Unit> = error("No usado")
        override suspend fun reiniciarProgreso(contrasena: String): Resultado<Unit> = error("No usado")
    }
}
