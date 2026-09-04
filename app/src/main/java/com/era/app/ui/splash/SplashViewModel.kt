package com.era.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.SesionRepository
import com.era.app.ui.navigation.EraRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sesionRepository: SesionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<SplashEvento>(Channel.BUFFERED)
    val eventos: Flow<SplashEvento> = _eventos.receiveAsFlow()

    init {
        decidirRutaInicial()
    }

    private fun decidirRutaInicial() {
        viewModelScope.launch {
            val evento = if (sesionRepository.tieneToken()) {
                _uiState.value = _uiState.value.copy(cargando = false)
                SplashEvento.NavegarAHome(EraRoutes.HOME)
            } else {
                _uiState.value = _uiState.value.copy(cargando = false)
                SplashEvento.NavegarALogin
            }
            _eventos.trySend(evento)
        }
    }
}
