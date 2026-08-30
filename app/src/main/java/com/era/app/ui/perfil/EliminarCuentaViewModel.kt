package com.era.app.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EliminarCuentaViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EliminarCuentaUiState())
    val uiState: StateFlow<EliminarCuentaUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<EliminarCuentaEvento>(Channel.BUFFERED)
    val eventos: Flow<EliminarCuentaEvento> = _eventos.receiveAsFlow()

    fun onContrasenaChange(nueva: String) {
        _uiState.update { it.copy(contrasena = nueva, errorGeneral = null) }
    }

    fun onToggleContrasenaVisible() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onEliminarClick() {
        if (_uiState.value.contrasena.isBlank()) return
        _uiState.update { it.copy(mostrarDialogoConfirmacion = true) }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(mostrarDialogoConfirmacion = false) }
    }

    fun confirmarEliminacion() {
        _uiState.update { it.copy(mostrarDialogoConfirmacion = false, cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            val resultado = userRepository.eliminarCuenta(_uiState.value.contrasena)
            when (resultado) {
                is Resultado.Exito -> {
                    sesionRepository.limpiarToken()
                    _uiState.update { it.copy(cargando = false) }
                    _eventos.trySend(EliminarCuentaEvento.NavegarALogin)
                }
                is Resultado.Fallo -> {
                    _uiState.update { it.copy(cargando = false, errorGeneral = resultado.error) }
                }
            }
        }
    }
}
