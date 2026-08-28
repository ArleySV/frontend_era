package com.era.app.repository

import com.era.app.remote.api.AuthApi
import com.era.app.remote.dto.auth.LoginRequest
import com.era.app.remote.dto.auth.RegisterRequest
import com.era.app.remote.dto.auth.ResendOtpRequest
import com.era.app.remote.dto.auth.VerifyEmailRequest
import com.era.app.utils.EraError
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: RemoteAuthRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repo = RemoteAuthRepository(retrofit.create(AuthApi::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun respuesta(status: Int, cuerpo: String): MockResponse =
        MockResponse().setResponseCode(status).setBody(cuerpo)

    private fun requestRegistro() = RegisterRequest(
        nombreMenor = "Ana",
        fechaNacimiento = "2016-05-10",
        nombreAcudiente = "María Pérez",
        cedulaAcudiente = "123456789",
        correo = "acudiente@test.com",
        nombreUsuario = "ana_p",
        avatar = "preset:1",
        contrasena = "<TEST_PASSWORD>",
        confirmarContrasena = "<TEST_PASSWORD>",
    )

    // ---------- register ----------

    @Test
    fun `register 201 mapea a Exito y envia ruta correcta`() = runTest {
        server.enqueue(respuesta(201, """{"message":"Registro exitoso"}"""))

        val r = repo.register(requestRegistro())

        assertTrue(r is Resultado.Exito)
        val peticion = server.takeRequest()
        assertEquals("POST /api/v1/auth/register", peticion.method + " " + peticion.path)
    }

    @Test
    fun `register 400 VALIDATION_ERROR mapea a Validacion con detalles`() = runTest {
        server.enqueue(
            respuesta(
                400,
                """
                {"timestamp":"2026-08-23T10:00:00Z","status":400,"error":"VALIDATION_ERROR",
                 "message":"Datos invalidos","path":"/api/v1/auth/register",
                 "details":[{"field":"contrasena","message":"La contrasena no cumple la politica"}]}
                """.trimIndent()
            )
        )

        val r = repo.register(requestRegistro())

        assertTrue(r is Resultado.Fallo)
        assertEquals(EraError.Validacion(listOf("La contrasena no cumple la politica")), (r as Resultado.Fallo).error)
    }

    @Test
    fun `register 409 EMAIL_ALREADY_REGISTERED mapea a CorreoRegistrado`() = runTest {
        server.enqueue(respuesta(409, cuerpoError(409, "EMAIL_ALREADY_REGISTERED")))

        val r = repo.register(requestRegistro())

        assertEquals(EraError.CorreoRegistrado, (r as Resultado.Fallo).error)
    }

    @Test
    fun `register 409 EMAIL_LOCKED mapea a CorreoBloqueado`() = runTest {
        server.enqueue(respuesta(409, cuerpoError(409, "EMAIL_LOCKED")))

        val r = repo.register(requestRegistro())

        assertEquals(EraError.CorreoBloqueado, (r as Resultado.Fallo).error)
    }

    @Test
    fun `register 409 CONFLICT mapea a UsuarioEnUso`() = runTest {
        server.enqueue(respuesta(409, cuerpoError(409, "CONFLICT")))

        val r = repo.register(requestRegistro())

        assertEquals(EraError.UsuarioEnUso, (r as Resultado.Fallo).error)
    }

    // ---------- verify-email ----------

    @Test
    fun `verifyEmail 200 mapea a Exito`() = runTest {
        server.enqueue(respuesta(200, """{"message":"Cuenta verificada"}"""))

        val r = repo.verifyEmail(VerifyEmailRequest(correo = "acudiente@test.com", codigo = "123456"))

        assertTrue(r is Resultado.Exito)
        assertEquals("/api/v1/auth/verify-email", server.takeRequest().path)
    }

    @Test
    fun `verifyEmail 401 OTP_INVALID_OR_EXPIRED mapea a OtpInvalido`() = runTest {
        server.enqueue(respuesta(401, cuerpoError(401, "OTP_INVALID_OR_EXPIRED")))

        val r = repo.verifyEmail(VerifyEmailRequest(correo = "acudiente@test.com", codigo = "000000"))

        assertEquals(EraError.OtpInvalido, (r as Resultado.Fallo).error)
    }

    // ---------- resend-otp ----------

    @Test
    fun `resendOtp 429 OTP_RESEND_THROTTLED mapea a ReenvioThrottled`() = runTest {
        server.enqueue(respuesta(429, cuerpoError(429, "OTP_RESEND_THROTTLED")))

        val r = repo.resendOtp(ResendOtpRequest(correo = "acudiente@test.com"))

        assertEquals(EraError.ReenvioThrottled, (r as Resultado.Fallo).error)
    }

    @Test
    fun `resendOtp 500 INTERNAL_ERROR mapea a ErrorServidor`() = runTest {
        server.enqueue(respuesta(500, cuerpoError(500, "INTERNAL_ERROR")))

        val r = repo.resendOtp(ResendOtpRequest(correo = "acudiente@test.com"))

        assertEquals(EraError.ErrorServidor, (r as Resultado.Fallo).error)
    }

    // ---------- casos de borde ----------

    @Test
    fun `codigo de error desconocido mapea a Desconocido con codigo http`() = runTest {
        server.enqueue(respuesta(503, cuerpoError(503, "SOME_NEW_CODE")))

        val r = repo.resendOtp(ResendOtpRequest(correo = "acudiente@test.com"))

        assertEquals(EraError.Desconocido(503), (r as Resultado.Fallo).error)
    }

    @Test
    fun `errorBody no parseable como JSON cae en Desconocido`() = runTest {
        server.enqueue(respuesta(400, "<html>Bad Request</html>"))

        val r = repo.register(requestRegistro())

        assertEquals(EraError.Desconocido(400), (r as Resultado.Fallo).error)
    }

    @Test
    fun `desconexion de red mapea a ErrorConexion`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val r = repo.register(requestRegistro())

        assertEquals(EraError.ErrorConexion, (r as Resultado.Fallo).error)
    }

    // ---------- login ----------

    private fun requestLogin() = LoginRequest(
        usuarioOCorreo = "usuario@test.com",
        contrasena = "<TEST_PASSWORD>",
    )

    @Test
    fun `login 200 mapea a Exito con token y envia ruta correcta`() = runTest {
        server.enqueue(respuesta(200, """{"token":"jwt_abc123"}"""))

        val r = repo.login(requestLogin())

        assertTrue(r is Resultado.Exito)
        assertEquals("jwt_abc123", (r as Resultado.Exito).data.token)
        val peticion = server.takeRequest()
        assertEquals("POST /api/v1/auth/login", peticion.method + " " + peticion.path)
    }

    @Test
    fun `login 401 INVALID_CREDENTIALS mapea a CredencialesInvalidas`() = runTest {
        server.enqueue(respuesta(401, cuerpoError(401, "INVALID_CREDENTIALS")))

        val r = repo.login(requestLogin())

        assertEquals(EraError.CredencialesInvalidas, (r as Resultado.Fallo).error)
    }

    @Test
    fun `login 423 ACCOUNT_LOCKED mapea a CuentaBloqueada`() = runTest {
        server.enqueue(respuesta(423, cuerpoError(423, "ACCOUNT_LOCKED")))

        val r = repo.login(requestLogin())

        assertEquals(EraError.CuentaBloqueada, (r as Resultado.Fallo).error)
    }

    @Test
    fun `login 403 ACCOUNT_INACTIVE mapea a CuentaInactiva`() = runTest {
        server.enqueue(respuesta(403, cuerpoError(403, "ACCOUNT_INACTIVE")))

        val r = repo.login(requestLogin())

        assertEquals(EraError.CuentaInactiva, (r as Resultado.Fallo).error)
    }

    @Test
    fun `login 400 VALIDATION_ERROR mapea a Validacion`() = runTest {
        server.enqueue(
            respuesta(
                400,
                """{"timestamp":"2026-08-23T10:00:00Z","status":400,"error":"VALIDATION_ERROR",
                   "message":"Datos invalidos","path":"/api/v1/auth/login",
                   "details":[{"field":"usuarioOCorreo","message":"No puede estar vacio"}]}"""
            )
        )

        val r = repo.login(requestLogin())

        assertTrue(r is Resultado.Fallo)
        assertTrue((r as Resultado.Fallo).error is EraError.Validacion)
    }

    @Test
    fun `login IOException mapea a ErrorConexion`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val r = repo.login(requestLogin())

        assertEquals(EraError.ErrorConexion, (r as Resultado.Fallo).error)
    }

    @Test
    fun `login request envia body correcto`() = runTest {
        server.enqueue(respuesta(200, """{"token":"jwt_xyz"}"""))

        repo.login(requestLogin())

        val body = server.takeRequest().body.readUtf8()
        assertTrue(body.contains("usuarioOCorreo"))
        assertTrue(body.contains("contrasena"))
    }

    private fun cuerpoError(status: Int, error: String): String =
        """{"timestamp":"2026-08-23T10:00:00Z","status":$status,"error":"$error",
           "message":"Mensaje generico","path":"/api/v1/auth/x"}"""
}
