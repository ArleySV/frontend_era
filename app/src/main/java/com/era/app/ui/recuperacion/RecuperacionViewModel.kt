package com.era.app.ui.recuperacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.era.app.repository.AuthRepository
import com.era.app.repository.Resultado
import com.era.app.remote.dto.auth.PasswordResetConfirmRequest
import com.era.app.remote.dto.auth.PasswordResetRequest
import com.era.app.remote.dto.auth.PasswordResetVerifyRequest
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
class RecuperacionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperacionUiState())
    val uiState: StateFlow<RecuperacionUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<RecuperacionEvento>(Channel.BUFFERED)
    val eventos: Flow<RecuperacionEvento> = _eventos.receiveAsFlow()

    private var countdownJob: Job? = null
    private var resetToken: String? = null

    // ---------- Paso 1: correo ----------

    fun onCorreoChange(valor: String) = actualizarCampo(
        limpiar = CampoRecuperacion.CORREO,
    ) { it.copy(correo = valor.trim()) }

    fun enviarEnlace() {
        val s = _uiState.value
        if (!Validators.isValidEmail(s.correo)) {
            _uiState.update {
                it.copy(errores = it.errores + CampoRecuperacion.CORREO, errorGeneral = null)
            }
            return
        }
        viewModelScope.launch {
            when (val r = authRepository.requestPasswordReset(
                PasswordResetRequest(correo = s.correo.trim())
            )) {
                is Resultado.Exito -> {
                    iniciarCountdownReenvio()
                    _eventos.trySend(RecuperacionEvento.NavegarAPaso2)
                }
                is Resultado.Fallo -> {
                    if (r.error is EraError.ReenvioThrottled) {
                        _uiState.update { it.copy(errorGeneral = r.error) }
                    } else {
                        manejarFallo(r.error)
                    }
                }
            }
        }
    }

    // ---------- Paso 2: verificación OTP ----------

    fun onCodigoOtpChange(valor: String) {
        val filtrado = valor.filter { it in '0'..'9' }.take(Validators.OTP_LENGTH)
        actualizarCampo(limpiar = CampoRecuperacion.CODIGO_OTP) {
            it.copy(codigoOtp = filtrado)
        }
    }

    fun verificarCodigo() {
        val correo = _uiState.value.correo.trim()
        if (correo.isEmpty()) {
            _eventos.trySend(RecuperacionEvento.ReiniciarFlujo)
            return
        }
        val s = _uiState.value
        if (!Validators.isValidOtp(s.codigoOtp)) {
            _uiState.update { it.copy(errores = it.errores + CampoRecuperacion.CODIGO_OTP) }
            return
        }
        viewModelScope.launch {
            when (val r = authRepository.verifyPasswordReset(
                PasswordResetVerifyRequest(correo = correo, codigo = s.codigoOtp)
            )) {
                is Resultado.Exito -> {
                    resetToken = r.data.resetToken
                    countdownJob?.cancel()
                    _eventos.trySend(RecuperacionEvento.NavegarAPaso3)
                }
                is Resultado.Fallo -> manejarFallo(r.error)
            }
        }
    }

    fun reenviarCodigo() {
        if (_uiState.value.reenvioSegundosRestantes > 0) return
        viewModelScope.launch {
            when (val r = authRepository.requestPasswordReset(
                PasswordResetRequest(correo = _uiState.value.correo.trim())
            )) {
                is Resultado.Exito -> iniciarCountdownReenvio()
                is Resultado.Fallo -> manejarFallo(r.error)
            }
        }
    }

    // ---------- Paso 3: nueva contraseña ----------

    fun onNuevaContrasenaChange(valor: String) = actualizarCampo(
        limpiar = CampoRecuperacion.NUEVA_CONTRASENA,
    ) { it.copy(nuevaContrasena = valor).conCriteriosActualizados() }

    fun onConfirmarContrasenaChange(valor: String) = actualizarCampo(
        limpiar = CampoRecuperacion.CONFIRMAR_CONTRASENA,
    ) { it.copy(confirmarContrasena = valor) }

    fun toggleNuevaContrasenaVisible() {
        _uiState.update { it.copy(nuevaContrasenaVisible = !it.nuevaContrasenaVisible) }
    }

    fun toggleConfirmarVisible() {
        _uiState.update { it.copy(confirmarVisible = !it.confirmarVisible) }
    }

    fun guardarContrasena() {
        val token = resetToken ?: run { _eventos.trySend(RecuperacionEvento.ReiniciarFlujo); return }
        val s = _uiState.value
        val fallos = buildSet {
            if (!PasswordPolicy.esValida(s.nuevaContrasena, "", "")) {
                add(CampoRecuperacion.NUEVA_CONTRASENA)
            }
            if (s.confirmarContrasena != s.nuevaContrasena) {
                add(CampoRecuperacion.CONFIRMAR_CONTRASENA)
            }
        }
        if (fallos.isNotEmpty()) {
            _uiState.update { it.copy(errores = it.errores + fallos, errorGeneral = null) }
            return
        }
        viewModelScope.launch {
            when (val r = authRepository.confirmPasswordReset(
                PasswordResetConfirmRequest(
                    resetToken = token,
                    nuevaContrasena = s.nuevaContrasena,
                    confirmarContrasena = s.confirmarContrasena,
                )
            )) {
                is Resultado.Exito -> {
                    resetToken = null
                    countdownJob?.cancel()
                    _eventos.trySend(RecuperacionEvento.RecuperacionExitosa)
                }
                is Resultado.Fallo -> manejarFallo(r.error)
            }
        }
    }

    fun cancelar() {
        countdownJob?.cancel()
        countdownJob = null
        resetToken = null
        _uiState.value = RecuperacionUiState()
    }

    // ---------- Internos ----------

    private fun manejarFallo(error: EraError) {
        when (error) {
            is EraError.ReenvioThrottled -> _eventos.trySend(RecuperacionEvento.Aviso(error))
            is EraError.ResetTokenInvalido -> reiniciarFlujo(mensaje = error)
            else -> _uiState.update { it.copy(errorGeneral = error) }
        }
    }

    private fun reiniciarFlujo(mensaje: EraError? = null) {
        resetToken = null
        countdownJob?.cancel()
        countdownJob = null
        _uiState.value = RecuperacionUiState(
            correo = _uiState.value.correo.trim(),
            errorGeneral = mensaje,
        )
        _eventos.trySend(RecuperacionEvento.ReiniciarFlujo)
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

    private fun RecuperacionUiState.conCriteriosActualizados(): RecuperacionUiState =
        copy(criteriosContrasena = PasswordPolicy.criterios(nuevaContrasena, "", ""))

    private inline fun actualizarCampo(
        limpiar: CampoRecuperacion,
        transform: (RecuperacionUiState) -> RecuperacionUiState,
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
    }
}