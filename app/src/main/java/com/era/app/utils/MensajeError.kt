package com.era.app.utils

import com.era.app.ui.recuperacion.CampoRecuperacion
import com.era.app.ui.register.CampoRegistro

fun EraError.mensajeUsuario(): String = when (this) {
    is EraError.Validacion -> detalles.ifEmpty { listOf("Error de validación") }.joinToString("\n")
    is EraError.CorreoRegistrado -> "Este correo ya tiene una cuenta"
    is EraError.CorreoBloqueado -> "Este correo no está disponible"
    is EraError.UsuarioEnUso -> "Este nombre de usuario ya está en uso"
    is EraError.OtpInvalido -> "Código inválido o expirado"
    is EraError.ReenvioThrottled -> "Debes esperar antes de reenviar"
    is EraError.ResetTokenInvalido -> "El enlace de recuperación expiró. Vuelve a solicitar un nuevo código"
    is EraError.PasswordReusada -> "No puedes repetir tu contraseña anterior"
    is EraError.ErrorServidor -> "Error del servidor. Intenta más tarde"
    is EraError.ErrorConexion -> "Sin conexión. Verifica tu internet"
    is EraError.Desconocido -> "Error inesperado"
    is EraError.CredencialesInvalidas -> "Correo/usuario o contraseña incorrectos"
    is EraError.CuentaBloqueada -> "Cuenta bloqueada temporalmente"
    is EraError.CuentaInactiva -> "Tu cuenta fue desactivada"
    is EraError.SesionExpirada -> "Tu sesión expiró. Vuelve a iniciar sesión"
    is EraError.PerfilNoEncontrado -> "No se pudo cargar el perfil"
}

fun mensajeCampo(campo: CampoRegistro): String = when (campo) {
    CampoRegistro.NOMBRE_MENOR -> "Ingresa el nombre del menor"
    CampoRegistro.FECHA_NACIMIENTO -> "Fecha inválida. La edad debe ser entre 7 y 11 años"
    CampoRegistro.NOMBRE_ACUDIENTE -> "Ingresa el nombre del acudiente"
    CampoRegistro.CEDULA_ACUDIENTE -> "Solo números, máximo 15 dígitos"
    CampoRegistro.CORREO -> "Ingresa un correo válido"
    CampoRegistro.NOMBRE_USUARIO -> "3-60 caracteres, sin espacios"
    CampoRegistro.AVATAR -> "Selecciona un avatar"
    CampoRegistro.CONTRASENA -> "La contraseña no cumple los requisitos"
    CampoRegistro.CONFIRMAR_CONTRASENA -> "Las contraseñas no coinciden"
    CampoRegistro.CODIGO_OTP -> "Ingresa 6 dígitos numéricos"
}

fun CampoRegistro.mensaje(
    errores: Set<CampoRegistro>,
    errorGeneral: EraError?,
): String? {
    if (this !in errores) return null
    return when {
        errorGeneral != null && this mapsTo errorGeneral -> errorGeneral.mensajeUsuario()
        else -> mensajeCampo(this)
    }
}

private infix fun CampoRegistro.mapsTo(error: EraError): Boolean = when (error) {
    is EraError.CorreoRegistrado,
    is EraError.CorreoBloqueado -> this == CampoRegistro.CORREO
    is EraError.UsuarioEnUso -> this == CampoRegistro.NOMBRE_USUARIO
    is EraError.OtpInvalido -> this == CampoRegistro.CODIGO_OTP
    is EraError.Validacion,
    is EraError.ReenvioThrottled,
    is EraError.ResetTokenInvalido,
    is EraError.PasswordReusada,
    is EraError.ErrorServidor,
    is EraError.ErrorConexion,
    is EraError.Desconocido,
    is EraError.CredencialesInvalidas,
    is EraError.CuentaBloqueada,
    is EraError.CuentaInactiva,
    is EraError.SesionExpirada,
    is EraError.PerfilNoEncontrado -> false
}

fun mensajeCampo(campo: CampoRecuperacion): String = when (campo) {
    CampoRecuperacion.CORREO -> "Ingresa un correo válido"
    CampoRecuperacion.CODIGO_OTP -> "Ingresa 6 dígitos numéricos"
    CampoRecuperacion.NUEVA_CONTRASENA -> "La contraseña no cumple los requisitos"
    CampoRecuperacion.CONFIRMAR_CONTRASENA -> "Las contraseñas no coinciden"
}

private infix fun CampoRecuperacion.mapsTo(error: EraError): Boolean = when (error) {
    is EraError.CorreoRegistrado,
    is EraError.CorreoBloqueado,
    is EraError.UsuarioEnUso,
    is EraError.OtpInvalido,
    is EraError.ReenvioThrottled,
    is EraError.ResetTokenInvalido,
    is EraError.PasswordReusada,
    is EraError.Validacion,
    is EraError.ErrorServidor,
    is EraError.ErrorConexion,
    is EraError.Desconocido,
    is EraError.CredencialesInvalidas,
    is EraError.CuentaBloqueada,
    is EraError.CuentaInactiva,
    is EraError.SesionExpirada,
    is EraError.PerfilNoEncontrado -> false
}

fun CampoRecuperacion.mensaje(
    errores: Set<CampoRecuperacion>,
    errorGeneral: EraError?,
): String? {
    if (this !in errores) return null
    return when {
        errorGeneral != null && this mapsTo errorGeneral -> errorGeneral.mensajeUsuario()
        else -> mensajeCampo(this)
    }
}
