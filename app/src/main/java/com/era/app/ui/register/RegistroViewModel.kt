package com.era.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.AuthRepository
import com.era.app.repository.Resultado
import com.era.app.remote.dto.auth.RegisterRequest
import com.era.app.remote.dto.auth.ResendOtpRequest
import com.era.app.remote.dto.auth.VerifyEmailRequest
import com.era.app.utils.EraError
import com.era.app.utils.PasswordPolicy
import com.era.app.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<RegistroEvento>(Channel.BUFFERED)
    val eventos: Flow<RegistroEvento> = _eventos.receiveAsFlow()

    private var countdownJob: Job? = null

    // ---------- Paso 1: datos de usuario ----------

    fun onNombreMenorChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.NOMBRE_MENOR,
    ) { it.copy(nombreMenor = valor) }

    fun onFechaNacimientoChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.FECHA_NACIMIENTO,
    ) { it.copy(fechaNacimientoDisplay = valor) }

    fun onNombreAcudienteChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.NOMBRE_ACUDIENTE,
    ) { it.copy(nombreAcudiente = valor) }

    fun onCedulaAcudienteChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.CEDULA_ACUDIENTE,
    ) { it.copy(cedulaAcudiente = valor) }

    fun continuarPaso1() {
        val s = _uiState.value
        val fallos = buildSet {
            if (s.nombreMenor.isBlank()) add(CampoRegistro.NOMBRE_MENOR)
            if (s.nombreAcudiente.isBlank()) add(CampoRegistro.NOMBRE_ACUDIENTE)
            if (!Validators.isValidCedulaUx(s.cedulaAcudiente)) add(CampoRegistro.CEDULA_ACUDIENTE)
            if (Validators.parseFechaNacimientoDesdeDisplay(s.fechaNacimientoDisplay) == null) {
                add(CampoRegistro.FECHA_NACIMIENTO)
            }
        }
        _uiState.update { it.copy(errores = it.errores + fallos, errorGeneral = null) }
        if (fallos.isEmpty()) _eventos.trySend(RegistroEvento.NavegarAPaso2)
    }

    fun cancelar() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = RegistroUiState()
    }

    // ---------- Paso 2: configurar cuenta ----------

    fun onCorreoChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.CORREO,
    ) { it.copy(correo = valor.trim()) }

    fun onNombreUsuarioChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.NOMBRE_USUARIO,
    ) { it.copy(nombreUsuario = valor).conCriteriosActualizados() }

    fun onAvatarSeleccionar(id: Int) {
        if (id !in AVATAR_PRESET_MIN..AVATAR_PRESET_MAX) return
        _uiState.update {
            it.copy(
                avatarSeleccionado = id,
                errores = it.errores - CampoRegistro.AVATAR,
                errorGeneral = null,
            )
        }
    }

    fun onContrasenaChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.CONTRASENA,
    ) { it.copy(contrasena = valor).conCriteriosActualizados() }

    fun onConfirmarContrasenaChange(valor: String) = actualizarCampo(
        limpiar = CampoRegistro.CONFIRMAR_CONTRASENA,
    ) { it.copy(confirmarContrasena = valor) }

    fun continuarPaso2() {
        val s = _uiState.value
        val fallos = buildSet {
            if (!Validators.isValidEmail(s.correo)) add(CampoRegistro.CORREO)
            if (!Validators.isValidNombreUsuario(s.nombreUsuario)) add(CampoRegistro.NOMBRE_USUARIO)
            if (s.avatarSeleccionado == null) add(CampoRegistro.AVATAR)
            if (!PasswordPolicy.esValida(s.contrasena, s.nombreUsuario, s.nombreMenor)) {
                add(CampoRegistro.CONTRASENA)
            }
            if (s.confirmarContrasena != s.contrasena) add(CampoRegistro.CONFIRMAR_CONTRASENA)
        }
        if (fallos.isNotEmpty()) {
            _uiState.update { it.copy(errores = it.errores + fallos, errorGeneral = null) }
            return
        }

        val fechaIso = Validators.parseFechaNacimientoDesdeDisplay(s.fechaNacimientoDisplay)
        if (fechaIso == null) {
            _uiState.update { it.copy(errores = it.errores + CampoRegistro.FECHA_NACIMIENTO) }
            return
        }

        val request = RegisterRequest(
            nombreMenor = s.nombreMenor.trim(),
            fechaNacimiento = fechaIso.toString(),
            nombreAcudiente = s.nombreAcudiente.trim(),
            cedulaAcudiente = s.cedulaAcudiente,
            correo = s.correo.trim(),
            nombreUsuario = s.nombreUsuario,
            avatar = "preset:${s.avatarSeleccionado}",
            contrasena = s.contrasena,
            confirmarContrasena = s.confirmarContrasena,
        )

        viewModelScope.launch {
            when (val r = authRepository.register(request)) {
                is Resultado.Exito -> {
                    iniciarCountdownReenvio()
                    _eventos.trySend(RegistroEvento.NavegarAPaso3)
                }
                is Resultado.Fallo -> manejarFalloDeCampos(r.error)
            }
        }
    }

    // ---------- Paso 3: verificación OTP ----------

    fun onCodigoOtpChange(valor: String) {
        val filtrado = valor.filter { it in '0'..'9' }.take(Validators.OTP_LENGTH)
        actualizarCampo(limpiar = CampoRegistro.CODIGO_OTP) {
            it.copy(codigoOtp = filtrado)
        }
    }

    fun verificarCodigo() {
        val s = _uiState.value
        if (!Validators.isValidOtp(s.codigoOtp)) {
            _uiState.update { it.copy(errores = it.errores + CampoRegistro.CODIGO_OTP) }
            return
        }
        viewModelScope.launch {
            when (val r = authRepository.verifyEmail(
                VerifyEmailRequest(correo = s.correo.trim(), codigo = s.codigoOtp)
            )) {
                is Resultado.Exito -> {
                    countdownJob?.cancel()
                    _eventos.trySend(RegistroEvento.RegistroVerificadoIrALogin)
                }
                is Resultado.Fallo -> manejarFalloDeCampos(r.error)
            }
        }
    }

    fun reenviarCodigo() {
        if (_uiState.value.reenvioSegundosRestantes > 0) return
        viewModelScope.launch {
            when (val r = authRepository.resendOtp(ResendOtpRequest(correo = _uiState.value.correo.trim()))) {
                is Resultado.Exito -> iniciarCountdownReenvio()
                is Resultado.Fallo -> manejarFalloDeAviso(r.error)
            }
        }
    }

    // ---------- Internos ----------

    private fun manejarFalloDeCampos(error: EraError) {
        when (error) {
            is EraError.CorreoRegistrado, is EraError.CorreoBloqueado -> _uiState.update {
                it.copy(
                    errores = it.errores + CampoRegistro.CORREO,
                    errorGeneral = error,
                )
            }
            is EraError.UsuarioEnUso -> _uiState.update {
                it.copy(
                    errores = it.errores + CampoRegistro.NOMBRE_USUARIO,
                    errorGeneral = error,
                )
            }
            is EraError.OtpInvalido -> _uiState.update {
                it.copy(
                    errores = it.errores + CampoRegistro.CODIGO_OTP,
                    errorGeneral = error,
                )
            }
            is EraError.Validacion -> _uiState.update {
                it.copy(errorGeneral = error)
            }
            else -> manejarFalloDeAviso(error)
        }
    }

    private fun manejarFalloDeAviso(error: EraError) {
        _eventos.trySend(RegistroEvento.Aviso(error))
    }

    private fun iniciarCountdownReenvio() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var restantes = REENVIO_SEGUNDOS
            while (restantes > 0) {
                _uiState.update { it.copy(reenvioSegundosRestantes = restantes) }
                delay(1_000)
                restantes--
            }
            _uiState.update { it.copy(reenvioSegundosRestantes = 0) }
        }
    }

    private fun RegistroUiState.conCriteriosActualizados(): RegistroUiState =
        copy(criteriosContrasena = PasswordPolicy.criterios(contrasena, nombreUsuario, nombreMenor))

    private inline fun actualizarCampo(
        limpiar: CampoRegistro,
        transform: (RegistroUiState) -> RegistroUiState,
    ) {
        _uiState.update {
            transform(it).copy(
                errores = it.errores - limpiar,
                errorGeneral = null,
            )
        }
    }

    private companion object {
        const val REENVIO_SEGUNDOS = 60
        const val AVATAR_PRESET_MIN = 1
        const val AVATAR_PRESET_MAX = 3
    }
}
