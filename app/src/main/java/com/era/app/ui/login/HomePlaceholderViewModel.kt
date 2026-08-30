package com.era.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.AuthRepository
import com.era.app.repository.SesionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomePlaceholderViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sesionRepository: SesionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomePlaceholderUiState())
    val uiState: StateFlow<HomePlaceholderUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<HomePlaceholderEvento>(Channel.BUFFERED)
    val eventos: Flow<HomePlaceholderEvento> = _eventos.receiveAsFlow()

    fun onCerrarSesionClick() {
        if (!_uiState.value.dialogoCierreVisible && !_uiState.value.cerrando) {
            _uiState.update { it.copy(dialogoCierreVisible = true) }
        }
    }

    fun onCancelarCierre() {
        if (!_uiState.value.cerrando) {
            _uiState.update { it.copy(dialogoCierreVisible = false) }
        }
    }

    fun onConfirmarCierre() {
        if (_uiState.value.cerrando) return
        _uiState.update { it.copy(cerrando = true) }
        viewModelScope.launch {
            authRepository.logout()
            sesionRepository.limpiarToken()
            _uiState.update { it.copy(cerrando = false, dialogoCierreVisible = false) }
            _eventos.trySend(HomePlaceholderEvento.NavegarALogin)
        }
    }
}