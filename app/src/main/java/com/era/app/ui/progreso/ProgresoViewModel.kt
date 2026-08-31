package com.era.app.ui.progreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.ProgresoRepository
import com.era.app.repository.Resultado
import com.era.app.utils.EraError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgresoViewModel @Inject constructor(
    private val progresoRepository: ProgresoRepository
) : ViewModel() {

    private val _sincronizando = MutableStateFlow(false)
    private val _error = MutableStateFlow<EraError?>(null)
    private val _dialogoResetVisible = MutableStateFlow(false)
    private val _contrasenaReset = MutableStateFlow("")
    private val _reseteando = MutableStateFlow(false)

    private val _eventos = Channel<ProgresoEvento>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    val uiState: StateFlow<ProgresoUiState> = combine(
        progresoRepository.obtenerNivelesConProgreso(),
        _sincronizando,
        _error,
        _dialogoResetVisible,
        _contrasenaReset,
        _reseteando
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val niveles = args[0] as List<com.era.app.data.model.NivelConProgreso>
        val sync = args[1] as Boolean
        val err = args[2] as EraError?
        val dialog = args[3] as Boolean
        val pass = args[4] as String
        val reseting = args[5] as Boolean

        val completados = niveles.count { it.estado == "COMPLETADO" }
        val reintentos = niveles.sumOf { it.intentosTotales }
        val totalNiveles = 20 // Catálogo fijo
        
        ProgresoUiState(
            niveles = niveles,
            sincronizando = sync,
            error = err,
            nivelesCompletados = completados,
            porcentaje = if (totalNiveles > 0) completados.toFloat() / totalNiveles else 0f,
            reintentosTotales = reintentos,
            dialogoResetVisible = dialog,
            contrasenaReset = pass,
            reseteando = reseting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgresoUiState(cargando = true)
    )

    init {
        sincronizar()
    }

    fun sincronizar() {
        if (_sincronizando.value) return
        
        _sincronizando.value = true
        viewModelScope.launch {
            when (val r = progresoRepository.sincronizarConServidor()) {
                is Resultado.Exito -> {
                    _error.value = null
                }
                is Resultado.Fallo -> {
                    if (r.error is EraError.SesionExpirada || r.error is EraError.CuentaInactiva) {
                        _eventos.send(ProgresoEvento.SesionExpirada)
                    } else {
                        _error.value = r.error
                        _eventos.send(ProgresoEvento.Error(r.error))
                    }
                }
            }
            _sincronizando.value = false
        }
    }

    fun onReiniciarProgresoClick() {
        _contrasenaReset.value = ""
        _dialogoResetVisible.value = true
    }

    fun onContrasenaResetChange(nueva: String) {
        _contrasenaReset.value = nueva
    }

    fun onCancelarReset() {
        if (_reseteando.value) return
        _dialogoResetVisible.value = false
    }

    fun onConfirmarReset() {
        val pass = _contrasenaReset.value
        if (pass.isBlank() || _reseteando.value) return
        
        _reseteando.value = true
        _error.value = null
        viewModelScope.launch {
            when (val r = progresoRepository.reiniciarProgreso(pass)) {
                is Resultado.Exito -> {
                    _dialogoResetVisible.value = false
                    _eventos.send(ProgresoEvento.ResetExitoso)
                }
                is Resultado.Fallo -> {
                    if (r.error is EraError.SesionExpirada || r.error is EraError.CuentaInactiva) {
                        _eventos.send(ProgresoEvento.SesionExpirada)
                    } else {
                        _error.value = r.error
                        _eventos.send(ProgresoEvento.Error(r.error))
                    }
                }
            }
            _reseteando.value = false
        }
    }
}
