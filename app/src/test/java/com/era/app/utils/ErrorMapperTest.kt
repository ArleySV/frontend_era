package com.era.app.utils

import com.era.app.remote.dto.common.ErrorResponse
import com.era.app.remote.dto.common.FieldError
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class ErrorMapperTest {

    private fun body(error: String, details: List<FieldError>? = null) = ErrorResponse(
        timestamp = "2026-08-23T10:00:00Z",
        status = 400,
        error = error,
        message = "mensaje generico",
        path = "/api/v1/auth/register",
        details = details
    )

    @Test
    fun `validation error mapea detalles a mensajes`() {
        val resultado = ErrorMapper.desdeHttp(
            400,
            body(
                "VALIDATION_ERROR",
                listOf(FieldError("contrasena", "Debe tener al menos 8 caracteres."))
            )
        )
        assertEquals(EraError.Validacion(listOf("Debe tener al menos 8 caracteres.")), resultado)
    }

    @Test
    fun `validation error sin detalles produce lista vacia`() {
        val resultado = ErrorMapper.desdeHttp(400, body("VALIDATION_ERROR"))
        assertEquals(EraError.Validacion(emptyList()), resultado)
    }

    @Test
    fun `codigos de negocio de fase uno mapean a su tipo`() {
        assertEquals(EraError.CorreoRegistrado, ErrorMapper.desdeHttp(409, body("EMAIL_ALREADY_REGISTERED")))
        assertEquals(EraError.CorreoBloqueado, ErrorMapper.desdeHttp(409, body("EMAIL_LOCKED")))
        assertEquals(EraError.UsuarioEnUso, ErrorMapper.desdeHttp(409, body("CONFLICT")))
        assertEquals(EraError.OtpInvalido, ErrorMapper.desdeHttp(401, body("OTP_INVALID_OR_EXPIRED")))
        assertEquals(EraError.ReenvioThrottled, ErrorMapper.desdeHttp(429, body("OTP_RESEND_THROTTLED")))
        assertEquals(EraError.ErrorServidor, ErrorMapper.desdeHttp(500, body("INTERNAL_ERROR")))
    }

    @Test
    fun `codigos de negocio de fase dos login mapean a su tipo`() {
        assertEquals(EraError.CredencialesInvalidas, ErrorMapper.desdeHttp(401, body("INVALID_CREDENTIALS")))
        assertEquals(EraError.CuentaBloqueada, ErrorMapper.desdeHttp(423, body("ACCOUNT_LOCKED")))
        assertEquals(EraError.CuentaInactiva, ErrorMapper.desdeHttp(403, body("ACCOUNT_INACTIVE")))
    }

    @Test
    fun `codigos de fase tres perfil mapean a su tipo`() {
        assertEquals(EraError.SesionExpirada, ErrorMapper.desdeHttp(401, body("UNAUTHORIZED")))
        assertEquals(EraError.PerfilNoEncontrado, ErrorMapper.desdeHttp(404, body("NOT_FOUND")))
    }

    @Test
    fun `INVALID_REQUEST mapea a Validacion con mensaje por defecto`() {
        val resultado = ErrorMapper.desdeHttp(400, body("INVALID_REQUEST"))
        assertEquals(EraError.Validacion(listOf("Solicitud inválida")), resultado)
    }

    @Test
    fun `CONFLICT sigue mapeando a UsuarioEnUso regresion registro`() {
        assertEquals(EraError.UsuarioEnUso, ErrorMapper.desdeHttp(409, body("CONFLICT")))
    }

    @Test
    fun `codigos de fase cinco recuperacion mapean a su tipo`() {
        assertEquals(EraError.ResetTokenInvalido, ErrorMapper.desdeHttp(401, body("RESET_TOKEN_INVALID")))
        assertEquals(EraError.PasswordReusada, ErrorMapper.desdeHttp(409, body("PASSWORD_REUSED")))
    }

    @Test
    fun `codigo desconocido conserva status y cuerpo nulo tambien`() {
        assertEquals(EraError.Desconocido(418), ErrorMapper.desdeHttp(418, body("TEAPOT")))
        assertEquals(EraError.Desconocido(503), ErrorMapper.desdeHttp(503, null))
    }

    @Test
    fun `ioexception mapea a error de conexion y otro throwable a desconocido`() {
        assertEquals(EraError.ErrorConexion, ErrorMapper.desdeThrowable(IOException("sin red")))
        assertEquals(EraError.Desconocido(null), ErrorMapper.desdeThrowable(IllegalStateException("x")))
    }
}
