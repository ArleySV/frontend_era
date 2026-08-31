package com.era.app.ui.faq

import com.era.app.data.model.FaqItem
import com.era.app.repository.FaqRepository
import com.era.app.repository.FeedbackRepository
import com.era.app.repository.Resultado
import com.era.app.utils.EraError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FaqViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var faqRepo: FakeFaqRepository
    private lateinit var feedbackRepo: FakeFeedbackRepository
    private lateinit var vm: FaqViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        faqRepo = FakeFaqRepository()
        feedbackRepo = FakeFeedbackRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init carga faqs correctamente`() = runTest {
        val faqs = listOf(FaqItem(1, "P1", "R1"))
        faqRepo.respuesta = Resultado.Exito(faqs)

        vm = FaqViewModel(faqRepo, feedbackRepo)
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()

        assertEquals(faqs, vm.uiState.value.faqs)
        assertFalse(vm.uiState.value.cargandoFaqs)
        job.cancel()
    }

    @Test
    fun `onComentarioChange actualiza estado y valida longitud`() = runTest {
        vm = FaqViewModel(faqRepo, feedbackRepo)
        
        vm.onComentarioChange("Hola")
        assertEquals("Hola", vm.uiState.value.comentario)
        assertTrue(vm.uiState.value.puedeEnviarComentario)

        // Simular 2001 caracteres
        vm.onComentarioChange("a".repeat(2001))
        assertFalse(vm.uiState.value.puedeEnviarComentario)
    }

    @Test
    fun `enviarComentario exito limpia campo y emite evento`() = runTest {
        feedbackRepo.respuesta = Resultado.Exito(Unit)
        vm = FaqViewModel(faqRepo, feedbackRepo)
        
        val eventos = mutableListOf<FaqEvento>()
        val jobEventos = launch { vm.eventos.collect { eventos.add(it) } }
        val jobUi = launch { vm.uiState.collect { } }

        vm.onComentarioChange("Sugerencia")
        vm.enviarComentario()
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.comentario)
        assertTrue(eventos.contains(FaqEvento.ComentarioEnviado))
        
        jobEventos.cancel()
        jobUi.cancel()
    }

    @Test
    fun `enviarComentario con sesion expirada emite SesionExpirada`() = runTest {
        feedbackRepo.respuesta = Resultado.Fallo(EraError.SesionExpirada)
        vm = FaqViewModel(faqRepo, feedbackRepo)
        
        val eventos = mutableListOf<FaqEvento>()
        val jobEventos = launch { vm.eventos.collect { eventos.add(it) } }

        vm.onComentarioChange("Test")
        vm.enviarComentario()
        advanceUntilIdle()

        assertTrue(eventos.contains(FaqEvento.SesionExpirada))
        jobEventos.cancel()
    }

    private class FakeFaqRepository : FaqRepository {
        var respuesta: Resultado<List<FaqItem>> = Resultado.Exito(emptyList())
        override suspend fun obtenerFaqs() = respuesta
    }

    private class FakeFeedbackRepository : FeedbackRepository {
        var respuesta: Resultado<Unit> = Resultado.Exito(Unit)
        override suspend fun enviarComentario(contenido: String) = respuesta
    }
}
