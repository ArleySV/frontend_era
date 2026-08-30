package com.era.app.repository

import com.era.app.remote.api.UsersApi
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class UserRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: RemoteUserRepository

    private val perfilJson = """
        {"nombreMenor":"María López","fechaNacimiento":"2016-05-10",
         "correo":"acudiente@test.com","nombreUsuario":"maria_lopez","avatar":"preset:1"}
    """.trimIndent()

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
        repo = RemoteUserRepository(retrofit.create(UsersApi::class.java), json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun respuesta(status: Int, cuerpo: String): MockResponse =
        MockResponse().setResponseCode(status).setBody(cuerpo)

    private fun cuerpoError(status: Int, error: String): String =
        """{"timestamp":"2026-08-28T10:00:00Z","status":$status,"error":"$error",
           "message":"Mensaje generico","path":"/api/v1/users/me"}"""

    // ---------- GET /users/me ----------

    @Test
    fun `GET 200 con cinco campos y preset mapea a Exito`() = runTest {
        server.enqueue(respuesta(200, perfilJson))

        val r = repo.obtenerPerfil()

        assertTrue(r is Resultado.Exito)
        val p = (r as Resultado.Exito).data
        assertEquals("María López", p.nombreMenor)
        assertEquals("2016-05-10", p.fechaNacimiento)
        assertEquals("acudiente@test.com", p.correo)
        assertEquals("maria_lopez", p.nombreUsuario)
        assertEquals("preset:1", p.avatar)
        val peticion = server.takeRequest()
        assertEquals("GET /api/v1/users/me", peticion.method + " " + peticion.path)
    }

    @Test
    fun `GET 200 con avatar nulo mapea a Exito con avatar nulo`() = runTest {
        val perfilSinAvatar = perfilJson.replace("\"preset:1\"", "null")
        server.enqueue(respuesta(200, perfilSinAvatar))

        val r = repo.obtenerPerfil()

        assertTrue(r is Resultado.Exito)
        assertNull((r as Resultado.Exito).data.avatar)
    }

    @Test
    fun `GET 401 UNAUTHORIZED mapea a SesionExpirada`() = runTest {
        server.enqueue(respuesta(401, cuerpoError(401, "UNAUTHORIZED")))

        val r = repo.obtenerPerfil()

        assertEquals(EraError.SesionExpirada, (r as Resultado.Fallo).error)
    }

    @Test
    fun `GET 403 ACCOUNT_INACTIVE mapea a CuentaInactiva`() = runTest {
        server.enqueue(respuesta(403, cuerpoError(403, "ACCOUNT_INACTIVE")))

        val r = repo.obtenerPerfil()

        assertEquals(EraError.CuentaInactiva, (r as Resultado.Fallo).error)
    }

    @Test
    fun `GET 404 NOT_FOUND mapea a PerfilNoEncontrado`() = runTest {
        server.enqueue(respuesta(404, cuerpoError(404, "NOT_FOUND")))

        val r = repo.obtenerPerfil()

        assertEquals(EraError.PerfilNoEncontrado, (r as Resultado.Fallo).error)
    }

    @Test
    fun `GET IOException mapea a ErrorConexion`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val r = repo.obtenerPerfil()

        assertEquals(EraError.ErrorConexion, (r as Resultado.Fallo).error)
    }

    // ---------- PATCH /users/me ----------

    @Test
    fun `PATCH 200 refleja el username nuevo en el cuerpo de respuesta`() = runTest {
        val perfilActualizado = perfilJson.replace("\"maria_lopez\"", "\"maria_nueva\"")
        server.enqueue(respuesta(200, perfilActualizado))

        val r = repo.actualizarNombreUsuario("maria_nueva")

        assertTrue(r is Resultado.Exito)
        assertEquals("maria_nueva", (r as Resultado.Exito).data.nombreUsuario)
    }

    @Test
    fun `PATCH 409 CONFLICT mapea a UsuarioEnUso`() = runTest {
        server.enqueue(respuesta(409, cuerpoError(409, "CONFLICT")))

        val r = repo.actualizarNombreUsuario("en_uso")

        assertEquals(EraError.UsuarioEnUso, (r as Resultado.Fallo).error)
    }

    @Test
    fun `PATCH 400 INVALID_REQUEST mapea a Validacion con mensaje por defecto`() = runTest {
        server.enqueue(respuesta(400, cuerpoError(400, "INVALID_REQUEST")))

        val r = repo.actualizarNombreUsuario("x")

        val error = (r as Resultado.Fallo).error
        assertTrue(error is EraError.Validacion)
        assertEquals(listOf("Solicitud inválida"), (error as EraError.Validacion).detalles)
    }

    @Test
    fun `PATCH 400 VALIDATION_ERROR con details mapea a Validacion con detalles`() = runTest {
        server.enqueue(
            respuesta(
                400,
                """{"timestamp":"2026-08-28T10:00:00Z","status":400,"error":"VALIDATION_ERROR",
                   "message":"Datos invalidos","path":"/api/v1/users/me",
                   "details":[{"field":"nombreUsuario","message":"3-60 caracteres, sin espacios"}]}"""
            )
        )

        val r = repo.actualizarNombreUsuario("x")

        val error = (r as Resultado.Fallo).error
        assertTrue(error is EraError.Validacion)
        assertEquals(listOf("3-60 caracteres, sin espacios"), (error as EraError.Validacion).detalles)
    }

    @Test
    fun `PATCH 401 UNAUTHORIZED mapea a SesionExpirada`() = runTest {
        server.enqueue(respuesta(401, cuerpoError(401, "UNAUTHORIZED")))

        val r = repo.actualizarNombreUsuario("nuevo")

        assertEquals(EraError.SesionExpirada, (r as Resultado.Fallo).error)
    }

    @Test
    fun `PATCH 403 ACCOUNT_INACTIVE mapea a CuentaInactiva`() = runTest {
        server.enqueue(respuesta(403, cuerpoError(403, "ACCOUNT_INACTIVE")))

        val r = repo.actualizarNombreUsuario("nuevo")

        assertEquals(EraError.CuentaInactiva, (r as Resultado.Fallo).error)
    }

    @Test
    fun `PATCH envia body solo con nombreUsuario`() = runTest {
        server.enqueue(respuesta(200, perfilJson))

        repo.actualizarNombreUsuario("maria_nueva")

        val peticion = server.takeRequest()
        assertEquals("PATCH /api/v1/users/me", peticion.method + " " + peticion.path)
        val body = peticion.body.readUtf8()
        assertTrue(body.contains("nombreUsuario"))
        assertTrue(body.contains("maria_nueva"))
        assertTrue(!body.contains("contrasena"))
        assertTrue(!body.contains("correo"))
    }

    // ---------- DELETE /users/me ----------

    @Test
    fun `DELETE 200 devuelve Exito(Unit)`() = runTest {
        server.enqueue(respuesta(200, """{"message":"Cuenta eliminada"}"""))

        val r = repo.eliminarCuenta("Contrasena123!")

        assertTrue(r is Resultado.Exito)
        val peticion = server.takeRequest()
        assertEquals("DELETE /api/v1/users/me", peticion.method + " " + peticion.path)
        val body = peticion.body.readUtf8()
        assertTrue(body.contains("contrasena"))
        assertTrue(body.contains("Contrasena123!"))
    }

    @Test
    fun `DELETE 401 INVALID_CREDENTIALS mapea a CredencialesInvalidas`() = runTest {
        server.enqueue(respuesta(401, cuerpoError(401, "INVALID_CREDENTIALS")))

        val r = repo.eliminarCuenta("wrong")

        assertEquals(EraError.CredencialesInvalidas, (r as Resultado.Fallo).error)
    }

    @Test
    fun `DELETE 401 UNAUTHORIZED mapea a SesionExpirada`() = runTest {
        server.enqueue(respuesta(401, cuerpoError(401, "UNAUTHORIZED")))

        val r = repo.eliminarCuenta("pass")

        assertEquals(EraError.SesionExpirada, (r as Resultado.Fallo).error)
    }

    @Test
    fun `DELETE 403 ACCOUNT_INACTIVE mapea a CuentaInactiva`() = runTest {
        server.enqueue(respuesta(403, cuerpoError(403, "ACCOUNT_INACTIVE")))

        val r = repo.eliminarCuenta("pass")

        assertEquals(EraError.CuentaInactiva, (r as Resultado.Fallo).error)
    }
}
