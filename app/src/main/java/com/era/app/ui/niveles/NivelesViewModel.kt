package com.era.app.ui.niveles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.ProgresoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class NivelesViewModel @Inject constructor(
    private val progresoRepository: ProgresoRepository,
) : ViewModel() {

    // Solo-lectura del catálogo Room: sin auto-sync ni DataStore (S3).
    val uiState: StateFlow<NivelesUiState> = progresoRepository
        .obtenerNivelesConProgreso()
        .map { niveles -> NivelesUiState(niveles = niveles, cargando = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NivelesUiState(),
        )

    private val _eventos = Channel<NivelesEvento>(Channel.BUFFERED)
    val eventos: Flow<NivelesEvento> = _eventos.receiveAsFlow()

    fun onNivelClick(orden: Int) {
        val nivel = uiState.value.niveles.firstOrNull { it.orden == orden } ?: return
        // Guard adicional al de la UI: bloqueado nunca navega.
        if (nivel.estado == "BLOQUEADO") return
        _eventos.trySend(NivelesEvento.NavegarAJuego(orden))
    }
}
