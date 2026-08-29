package com.era.app.utils

sealed class EraError {
    data class Validacion(val detalles: List<String>) : EraError()
    data object CorreoRegistrado : EraError()
    data object CorreoBloqueado : EraError()
    data object UsuarioEnUso : EraError()
    data object OtpInvalido : EraError()
    data object ReenvioThrottled : EraError()
    data object CredencialesInvalidas : EraError()
    data object CuentaBloqueada : EraError()
    data object CuentaInactiva : EraError()
    data object SesionExpirada : EraError()
    data object PerfilNoEncontrado : EraError()
    data object ErrorServidor : EraError()
    data object ErrorConexion : EraError()
    data class Desconocido(val codigoHttp: Int?) : EraError()
}
