package com.era.app.utils

import com.era.app.remote.dto.common.ErrorResponse
import java.io.IOException

object ErrorMapper {

    fun desdeHttp(status: Int, body: ErrorResponse?): EraError {
        if (body == null) return EraError.Desconocido(status)
        return when (body.error) {
            "VALIDATION_ERROR" -> EraError.Validacion(
                detalles = body.details.orEmpty().map { it.message }
            )
            "EMAIL_ALREADY_REGISTERED" -> EraError.CorreoRegistrado
            "EMAIL_LOCKED" -> EraError.CorreoBloqueado
            "CONFLICT" -> EraError.UsuarioEnUso
            "OTP_INVALID_OR_EXPIRED" -> EraError.OtpInvalido
            "OTP_RESEND_THROTTLED" -> EraError.ReenvioThrottled
            "INVALID_CREDENTIALS" -> EraError.CredencialesInvalidas
            "ACCOUNT_LOCKED" -> EraError.CuentaBloqueada
            "ACCOUNT_INACTIVE" -> EraError.CuentaInactiva
            "INTERNAL_ERROR" -> EraError.ErrorServidor
            else -> EraError.Desconocido(status)
        }
    }

    fun desdeThrowable(t: Throwable): EraError = when (t) {
        is IOException -> EraError.ErrorConexion
        else -> EraError.Desconocido(null)
    }
}
