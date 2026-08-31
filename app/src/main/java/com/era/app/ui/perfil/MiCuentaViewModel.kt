package com.era.app.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.repository.UserRepository
import com.era.app.ui.register.CampoRegistro
import com.era.app.utils.EraError
import com.era.app.utils.Validators
import com.era.app.utils.mensajeCampo
import com.era.app.utils.mensajeUsuario
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
class MiCuentaViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sesionRepository: SesionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiCuentaUiState())
    val uiState: StateFlow<MiCuentaUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<MiCuentaEvento>(Channel.BUFFERED)
    val eventos: Flow<MiCuentaEvento> = _eventos.receiveAsFlow()

    fun onEntrar() {
        val s = _uiState.value
        if (s.cargando) return
        cargarPerfil()
    }

    private fun cargarPerfil() {
        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = userRepository.obtenerPerfil()) {
                is Resultado.Exito -> {
                    sesionRepository.guardarCorreo(r.data.correo)
                    _uiState.update { it.copy(cargando = false, perfil = r.data) }
                }
                is Resultado.Fallo -> manejarFallo(r.error)
            }
        }
    }

    fun onReintentar() = cargarPerfil()

    fun onEditarClick() {
        val p = _uiState.value.perfil ?: return
        _uiState.update {
            it.copy(
                dialogoAbierto = true,
                nombreUsuario = p.nombreUsuario,
                errorNombreUsuario = null,
            )
        }
    }

    fun onDialogCancelar() {
        _uiState.update {
            it.copy(dialogoAbierto = false, nombreUsuario = "", errorNombreUsuario = null)
        }
    }

    fun onNombreUsuarioChange(valor: String) {
        _uiState.update { it.copy(nombreUsuario = valor, errorNombreUsuario = null) }
    }

    fun onGuardarClick() {
        val s = _uiState.value
        val nombre = s.nombreUsuario
        if (s.guardando) return

        if (!Validators.isValidNombreUsuario(nombre)) {
            _uiState.update { it.copy(errorNombreUsuario = mensajeCampo(CampoRegistro.NOMBRE_USUARIO)) }
            return
        }

        _uiState.update { it.copy(guardando = true, errorNombreUsuario = null) }
        viewModelScope.launch {
            when (val r = userRepository.actualizarNombreUsuario(nombre)) {
                is Resultado.Exito -> {
                    sesionRepository.guardarCorreo(r.data.correo)
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            perfil = r.data,
                            dialogoAbierto = false,
                            nombreUsuario = "",
                            errorNombreUsuario = null,
                        )
                    }
                }
                is Resultado.Fallo -> manejarFalloGuardar(r.error)
            }
        }
    }

    private fun manejarFallo(error: EraError) {
        when (error) {
            is EraError.CuentaInactiva,
            is EraError.SesionExpirada -> cerrarSesionPorReglaCinco()
            else -> _uiState.update { it.copy(cargando = false, errorGeneral = error) }
        }
    }

    private fun manejarFalloGuardar(error: EraError) {
        when (error) {
            is EraError.UsuarioEnUso -> {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorNombreUsuario = error.mensajeUsuario(),
                    )
                }
            }
            is EraError.CuentaInactiva,
            is EraError.SesionExpirada -> cerrarSesionPorReglaCinco()
            else -> {
                _uiState.update { it.copy(guardando = false) }
                _eventos.trySend(MiCuentaEvento.MostrarSnackbar(error.mensajeUsuario()))
            }
        }
    }

    private fun cerrarSesionPorReglaCinco() {
        sesionRepository.limpiarToken()
        _uiState.update { it.copy(cargando = false, guardando = false) }
        _eventos.trySend(MiCuentaEvento.NavegarALogin)
    }
}
