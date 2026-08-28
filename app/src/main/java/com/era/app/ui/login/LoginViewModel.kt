package com.era.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.remote.dto.auth.LoginRequest
import com.era.app.repository.AuthRepository
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.utils.EraError
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sesionRepository: SesionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<LoginEvento>(Channel.BUFFERED)
    val eventos: Flow<LoginEvento> = _eventos.receiveAsFlow()

    fun onUsuarioOCorreoChange(valor: String) {
        _uiState.update {
            it.copy(
                usuarioOCorreo = valor,
                campoConError = null,
                errorGeneral = null,
            )
        }
    }

    fun onContrasenaChange(valor: String) {
        _uiState.update {
            it.copy(
                contrasena = valor,
                campoConError = null,
                errorGeneral = null,
            )
        }
    }

    fun onContrasenaVisibleToggle() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onLoginClick() {
        val s = _uiState.value
        val usuarioLimpio = s.usuarioOCorreo.trim()

        if (usuarioLimpio.isBlank() || s.contrasena.isBlank()) {
            _uiState.update {
                it.copy(
                    campoConError = when {
                        usuarioLimpio.isBlank() -> CampoLogin.USUARIO_O_CORREO
                        s.contrasena.isBlank() -> CampoLogin.CONTRASENA
                        else -> null
                    },
                )
            }
            return
        }

        _uiState.update { it.copy(cargando = true, errorGeneral = null, campoConError = null) }

        viewModelScope.launch {
            when (val r = authRepository.login(
                LoginRequest(usuarioOCorreo = usuarioLimpio, contrasena = s.contrasena)
            )) {
                is Resultado.Exito -> {
                    sesionRepository.guardarToken(r.data.token)
                    _uiState.update { it.copy(cargando = false) }
                    _eventos.trySend(LoginEvento.NavegarAHome)
                }
                is Resultado.Fallo -> manejarFallo(r.error)
            }
        }
    }

    fun onNavegarARegistro() {
        _eventos.trySend(LoginEvento.NavegarARegistro)
    }

    fun onOlvidasteContrasena() {
        _eventos.trySend(LoginEvento.MostrarSnackbar("Próximamente"))
    }

    private fun manejarFallo(error: EraError) {
        when (error) {
            is EraError.CuentaInactiva -> {
                sesionRepository.limpiarToken()
                _uiState.update { it.copy(cargando = false, errorGeneral = error) }
                _eventos.trySend(LoginEvento.NavegarALogin)
            }
            else -> {
                _uiState.update { it.copy(cargando = false, errorGeneral = error) }
            }
        }
    }
}
