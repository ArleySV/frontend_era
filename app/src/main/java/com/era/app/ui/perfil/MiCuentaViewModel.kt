package com.era.app.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.AvatarRepository
import com.era.app.repository.Resultado
import com.era.app.repository.SesionRepository
import com.era.app.repository.UserRepository
import com.era.app.ui.register.CampoRegistro
import com.era.app.utils.ArchivoAvatar
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
    private val avatarRepository: AvatarRepository,
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
                    cargarAvatarSiCustom(r.data.avatar)
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

    private fun cargarAvatarSiCustom(avatar: String?) {
        if (!esAvatarCustom(avatar)) return
        viewModelScope.launch {
            when (val r = avatarRepository.obtenerAvatarBytes()) {
                is Resultado.Exito -> {
                    _uiState.update { it.copy(bytesAvatarPersonalizado = r.data) }
                }
                is Resultado.Fallo -> {
                    // 404 (PerfilNoEncontrado) o fallo de red al precargar: no bloqueamos la
                    // pantalla; el avatar sigue en presets/iniciales. Solo se cierra sesión
                    // ante 401/403 reales.
                    when (r.error) {
                        is EraError.CuentaInactiva,
                        is EraError.SesionExpirada -> cerrarSesionPorReglaCinco()
                        else -> Unit
                    }
                }
            }
        }
    }

    fun onCambiarAvatarClick() {
        val p = _uiState.value.perfil ?: return
        val presetActual = extraerPresetId(p.avatar)
        _uiState.update {
            it.copy(selectorAvatarAbierto = true, avatarPresetSeleccionado = presetActual, errorAvatar = null)
        }
    }

    fun onSeleccionarPreset(id: Int) {
        // Vuelta a preset LOCAL (D-58): no persiste en servidor (backend sin endpoint de reset).
        val perfil = _uiState.value.perfil
        _uiState.update {
            it.copy(
                avatarPresetSeleccionado = id,
                bytesAvatarPersonalizado = null,
                selectorAvatarAbierto = false,
                subiendoAvatar = false,
                errorAvatar = null,
            )
        }
        // Reflejar el preset en el perfil local para que AvatarPerfil lo dibuje.
        if (perfil != null) {
            _uiState.update { s -> s.copy(perfil = perfil.copy(avatar = "preset:$id")) }
        }
    }

    fun onAvatarSeleccionado(validacion: Resultado<ArchivoAvatar>) {
        if (_uiState.value.subiendoAvatar) return

        when (validacion) {
            is Resultado.Fallo -> {
                _uiState.update { it.copy(errorAvatar = validacion.error, selectorAvatarAbierto = false) }
            }
            is Resultado.Exito -> {
                subirAvatar(validacion.data.bytes, validacion.data.filename, validacion.data.mimeType)
            }
        }
    }

    private fun subirAvatar(bytes: ByteArray, filename: String?, mimeType: String) {
        _uiState.update { it.copy(subiendoAvatar = true, errorAvatar = null) }
        viewModelScope.launch {
            when (val r = avatarRepository.subirAvatar(bytes, filename, mimeType)) {
                is Resultado.Exito -> {
                    sesionRepository.guardarCorreo(_uiState.value.perfil?.correo.orEmpty())
                    _uiState.update {
                        it.copy(
                            subiendoAvatar = false,
                            selectorAvatarAbierto = false,
                            bytesAvatarPersonalizado = bytes,
                        )
                    }
                    // Round-trip GET /me para sincronizar avatar: custom:* persistido (D-56).
                    cargarPerfil()
                }
                is Resultado.Fallo -> {
                    _uiState.update { it.copy(subiendoAvatar = false, selectorAvatarAbierto = false) }
                    manejarFalloAvatar(r.error)
                }
            }
        }
    }

    private fun manejarFalloAvatar(error: EraError) {
        when (error) {
            is EraError.CuentaInactiva,
            is EraError.SesionExpirada -> cerrarSesionPorReglaCinco()
            else -> _uiState.update { it.copy(errorAvatar = error) }
        }
    }

    fun onCerrarSelector() {
        _uiState.update { it.copy(selectorAvatarAbierto = false, errorAvatar = null) }
    }

    fun onLimpiarErrorAvatar() {
        _uiState.update { it.copy(errorAvatar = null) }
    }

    private fun cerrarSesionPorReglaCinco() {
        sesionRepository.limpiarToken()
        _uiState.update { it.copy(cargando = false, guardando = false) }
        _eventos.trySend(MiCuentaEvento.NavegarALogin)
    }

    private fun esAvatarCustom(avatar: String?): Boolean = avatar?.startsWith("custom:") == true

    private fun extraerPresetId(avatar: String?): Int? = when (avatar) {
        "preset:1" -> 1
        "preset:2" -> 2
        "preset:3" -> 3
        else -> null
    }
}
