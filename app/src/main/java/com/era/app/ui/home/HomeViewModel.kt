package com.era.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.AuthRepository
import com.era.app.repository.AvatarRepository
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.repository.UserRepository
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
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sesionRepository: SesionRepository,
    private val userRepository: UserRepository,
    private val avatarRepository: AvatarRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<HomeEvento>(Channel.BUFFERED)
    val eventos: Flow<HomeEvento> = _eventos.receiveAsFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        _uiState.update { it.copy(cargandoPerfil = true) }
        viewModelScope.launch {
            when (val r = userRepository.obtenerPerfil()) {
                is Resultado.Exito -> {
                    // Persiste el userId (correo) en la primera pantalla autenticada
                    // garantizada: sin esto, obtenerNivelesConProgreso() devuelve
                    // lista vacía si el usuario no pasó antes por "Mi cuenta"
                    // (patrón MiCuentaViewModel; fix bug S3).
                    sesionRepository.guardarCorreo(r.data.correo)
                    _uiState.update {
                        it.copy(
                            nombreMenor = r.data.nombreMenor,
                            correo = r.data.correo,
                            avatar = r.data.avatar,
                            bytesAvatarCustom = null,
                            cargandoPerfil = false,
                        )
                    }
                    // Avatar subido (custom:*): el binario vive en GET /users/me/avatar
                    // (requiere JWT), no en el campo `avatar` del perfil. Descarga
                    // best-effort para la cabecera del drawer (patrón MiCuentaViewModel).
                    if (r.data.avatar?.startsWith("custom:") == true) {
                        descargarAvatarCustom()
                    }
                }
                is Resultado.Fallo -> {
                    // Offline-first: saludo y cabecera caen a genérico sin bloquear (REQ-NF-01).
                    _uiState.update {
                        it.copy(
                            nombreMenor = "",
                            correo = sesionRepository.obtenerCorreo() ?: "",
                            avatar = null,
                            bytesAvatarCustom = null,
                            cargandoPerfil = false,
                        )
                    }
                }
            }
        }
    }

    private fun descargarAvatarCustom() {
        viewModelScope.launch {
            when (val r = avatarRepository.obtenerAvatarBytes()) {
                is Resultado.Exito -> _uiState.update { it.copy(bytesAvatarCustom = r.data) }
                is Resultado.Fallo -> Unit // best-effort: el drawer cae a presets/iniciales
            }
        }
    }

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
            _eventos.trySend(HomeEvento.NavegarALogin)
        }
    }
}
