package com.era.app.ui.juego

import androidx.lifecycle.SavedStateHandle
import com.era.app.data.model.NivelConProgreso
import com.era.app.repository.ProgresoRepository
import com.era.app.repository.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JuegoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeProgresoRepository
    private lateinit var vm: JuegoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeProgresoRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearVm(orden: Int): JuegoViewModel =
        JuegoViewModel(repo, SavedStateHandle(mapOf("nivelOrden" to orden)))

    @Test
    fun `carga el nivel del repo y arranca JUGANDO con 15 segundos`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(3, "DISPONIBLE"))
        vm = crearVm(3)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertEquals(FaseJuego.JUGANDO, s.fase)
        assertEquals(3, s.nivel?.orden)
        assertEquals(15, s.segundosRestantes)
    }

    @Test
    fun `el cronometro decrementa cada segundo hasta 0`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(1, "DISPONIBLE"))
        vm = crearVm(1)
        advanceUntilIdle()

        val s = vm.uiState.value
        assertEquals(FaseJuego.RESULTADO, s.fase)
        assertEquals(false, s.resultadoCorrecto)
    }

    @Test
    fun `respuesta correcta registra exito y navega al siguiente nivel`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(1, "DISPONIBLE"), nivel(2, "DISPONIBLE"))
        vm = crearVm(1)
        val eventos = mutableListOf<JuegoEvento>()
        val job = launch { vm.eventos.collect { eventos.add(it) } }
        advanceTimeBy(1)

        vm.onOpcionClick(1)
        advanceUntilIdle()

        assertEquals(listOf(1 to true), repo.resultadosRegistrados)
        assertTrue(eventos.any { it is JuegoEvento.NavegarANiveles && (it as JuegoEvento.NavegarANiveles).orden == 2 })
        job.cancel()
    }

    @Test
    fun `respuesta correcta en ultimo nivel navega a Niveles`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(20, "DISPONIBLE"))
        vm = crearVm(20)
        val eventos = mutableListOf<JuegoEvento>()
        val job = launch { vm.eventos.collect { eventos.add(it) } }
        advanceTimeBy(1)

        vm.onOpcionClick(1)
        advanceUntilIdle()

        assertTrue(eventos.any { it is JuegoEvento.VolverANiveles })
        job.cancel()
    }

    @Test
    fun `respuesta incorrecta registra fallo y tras 3s reinicia la misma pregunta`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(1, "DISPONIBLE"))
        vm = crearVm(1)
        advanceTimeBy(1)

        vm.onOpcionClick(0)
        advanceTimeBy(1)
        assertEquals(listOf(1 to false), repo.resultadosRegistrados)
        assertEquals(FaseJuego.RESULTADO, vm.uiState.value.fase)
        vm.onSalir()
        advanceUntilIdle()
    }

    @Test
    fun `cronometro a cero sin responder procesa como incorrecta`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(1, "DISPONIBLE"))
        vm = crearVm(1)
        advanceTimeBy(1)

        advanceTimeBy(15_000)
        assertEquals(listOf(1 to false), repo.resultadosRegistrados)
        assertEquals(FaseJuego.RESULTADO, vm.uiState.value.fase)
        vm.onSalir()
        advanceUntilIdle()
    }

    @Test
    fun `dos fallos consecutivos activan pausa de 60s`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(1, "DISPONIBLE", fallosConsecutivos = 1))
        vm = crearVm(1)
        advanceTimeBy(1)

        vm.onOpcionClick(0)
        advanceTimeBy(1)
        assertEquals(FaseJuego.PAUSA, vm.uiState.value.fase)
        assertEquals(60, vm.uiState.value.segundosPausa)
        vm.onSalir()
        advanceUntilIdle()
    }

    @Test
    fun `menu se superpone sin pausar el cronometro y salir vuelve a Niveles`() = runTest {
        repo.nivelesFlow.value = listaNiveles()
        vm = crearVm(1)
        val eventos = mutableListOf<JuegoEvento>()
        val job = launch { vm.eventos.collect { eventos.add(it) } }
        advanceTimeBy(1)

        vm.onAbrirMenu()
        assertEquals(FaseJuego.MENU, vm.uiState.value.fase)

        advanceTimeBy(2000)
        assertEquals(13, vm.uiState.value.segundosRestantes)

        vm.onContinuar()
        assertEquals(FaseJuego.JUGANDO, vm.uiState.value.fase)
        assertEquals(13, vm.uiState.value.segundosRestantes)

        vm.onAbrirMenu()
        vm.onSalir()
        advanceUntilIdle()
        assertTrue(eventos.any { it is JuegoEvento.VolverANiveles })
        job.cancel()
    }

    @Test
    fun `mensajeResultado se establece al responder correcto`() = runTest {
        repo.nivelesFlow.value = listaNiveles()
        vm = crearVm(1)
        advanceTimeBy(1)

        vm.onOpcionClick(1)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.mensajeResultado.isNotEmpty())
        assertTrue(JuegoViewModel.FRASES_FELICITACION.contains(vm.uiState.value.mensajeResultado))
    }

    @Test
    fun `mensajeResultado se establece al responder incorrecto`() = runTest {
        repo.nivelesFlow.value = listaNiveles()
        vm = crearVm(1)
        advanceTimeBy(1)

        vm.onOpcionClick(0)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.mensajeResultado.isNotEmpty())
        assertTrue(JuegoViewModel.FRASES_MOTIVACION.contains(vm.uiState.value.mensajeResultado))
    }

    @Test
    fun `fraseSabia se establece al entrar en pausa`() = runTest {
        repo.nivelesFlow.value = listOf(nivel(1, "DISPONIBLE", fallosConsecutivos = 1))
        vm = crearVm(1)
        advanceTimeBy(1)

        vm.onOpcionClick(0)
        advanceTimeBy(1)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.fraseSabia.isNotEmpty())
        assertTrue(JuegoViewModel.FRASES_SABIAS.contains(vm.uiState.value.fraseSabia))
        assertEquals(FaseJuego.PAUSA, vm.uiState.value.fase)
    }

    private fun listaNiveles() = listOf(
        nivel(1, "DISPONIBLE"), nivel(2, "DISPONIBLE"), nivel(3, "DISPONIBLE"),
        nivel(4, "DISPONIBLE"), nivel(5, "DISPONIBLE"), nivel(6, "DISPONIBLE"),
        nivel(7, "DISPONIBLE"), nivel(8, "DISPONIBLE"), nivel(9, "DISPONIBLE"),
        nivel(10, "DISPONIBLE"), nivel(11, "DISPONIBLE"), nivel(12, "DISPONIBLE"),
        nivel(13, "DISPONIBLE"), nivel(14, "DISPONIBLE"), nivel(15, "DISPONIBLE"),
        nivel(16, "DISPONIBLE"), nivel(17, "DISPONIBLE"), nivel(18, "DISPONIBLE"),
        nivel(19, "DISPONIBLE"), nivel(20, "DISPONIBLE"),
    )

    private fun nivel(orden: Int, estado: String, fallosConsecutivos: Int = 0) = NivelConProgreso(
        orden = orden,
        pregunta = "P$orden",
        opcionA = "A", opcionB = "B", opcionC = "C",
        respuestaCorrecta = 1,
        estado = estado,
        intentosTotales = 0,
        intentosFallidosConsecutivos = fallosConsecutivos,
        completadoEn = null,
        sincronizado = true,
    )

    private class FakeProgresoRepository : ProgresoRepository {
        val nivelesFlow = MutableStateFlow<List<NivelConProgreso>>(emptyList())
        val resultadosRegistrados = mutableListOf<Pair<Int, Boolean>>()

        override fun obtenerNivelesConProgreso(): Flow<List<NivelConProgreso>> = nivelesFlow

        override suspend fun registrarResultado(orden: Int, exito: Boolean): Resultado<Unit> {
            resultadosRegistrados.add(orden to exito)
            nivelesFlow.value = nivelesFlow.value.map {
                if (it.orden == orden) {
                    it.copy(
                        intentosFallidosConsecutivos = if (exito) 0 else it.intentosFallidosConsecutivos + 1,
                        estado = if (exito) "COMPLETADO" else it.estado,
                    )
                } else {
                    it
                }
            }
            return Resultado.Exito(Unit)
        }

        override suspend fun sincronizarConServidor(): Resultado<Unit> = error("No usado")
        override suspend fun reiniciarProgreso(contrasena: String): Resultado<Unit> = error("No usado")
    }
}