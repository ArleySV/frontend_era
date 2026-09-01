package com.era.app.ui.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.FaqRepository
import com.era.app.repository.FeedbackRepository
import com.era.app.repository.Resultado
import com.era.app.utils.EraError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaqViewModel @Inject constructor(
    private val faqRepository: FaqRepository,
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FaqUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventos = Channel<FaqEvento>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    init {
        cargarFaqs()
    }

    fun cargarFaqs() {
        _uiState.update { it.copy(cargandoFaqs = true, errorFaqs = null) }
        viewModelScope.launch {
            when (val r = faqRepository.obtenerFaqs()) {
                is Resultado.Exito -> _uiState.update { 
                    it.copy(faqs = r.data, cargandoFaqs = false) 
                }
                is Resultado.Fallo -> _uiState.update { 
                    it.copy(errorFaqs = r.error, cargandoFaqs = false) 
                }
            }
        }
    }

    fun onComentarioChange(nuevo: String) {
        // Limitación local preventiva de UI (límite oficial 2000)
        if (nuevo.length <= 2000) {
            _uiState.update { it.copy(comentario = nuevo, errorComentario = null) }
        }
    }

    fun enviarComentario() {
        val contenido = _uiState.value.comentario
        if (contenido.isBlank() || contenido.length > 2000) return

        _uiState.update { it.copy(enviandoComentario = true, errorComentario = null) }
        
        viewModelScope.launch {
            // NO LOGUEAR 'contenido' (Regla de Oro)
            when (val r = feedbackRepository.enviarComentario(contenido.trim())) {
                is Resultado.Exito -> {
                    _uiState.update { it.copy(comentario = "", enviandoComentario = false) }
                    _eventos.send(FaqEvento.ComentarioEnviado)
                }
                is Resultado.Fallo -> {
                    if (r.error is EraError.SesionExpirada || r.error is EraError.CuentaInactiva) {
                        _eventos.send(FaqEvento.SesionExpirada)
                    } else {
                        _uiState.update { it.copy(enviandoComentario = false, errorComentario = r.error) }
                        _eventos.send(FaqEvento.Error(r.error))
                    }
                }
            }
        }
    }
}
