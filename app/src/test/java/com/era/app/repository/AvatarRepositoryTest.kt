package com.era.app.repository

import com.era.app.remote.api.AvatarApi
import com.era.app.utils.EraError
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AvatarRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: RemoteAvatarRepository
    private lateinit var fakeSesion: FakeSesionRepository
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(AvatarApi::class.java)
        fakeSesion = FakeSesionRepository()
        repo = RemoteAvatarRepository(api, fakeSesion, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private val bytesOk = byteArrayOf(1, 2, 3, 4)

    // ---------- subirAvatar ----------

    @Test
    fun `subirAvatar exito retorna Unit y envia filename en multipart`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"message": "OK"}"""))

        val resultado = repo.subirAvatar(bytesOk, "foto.png", "image/png")

        assertTrue(resultado is Resultado.Exito)
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("""name="avatar""""))
        assertTrue(body.contains("""filename="foto.png""""))
    }

    @Test
    fun `subirAvatar con filename nulo usa extension del mime`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"message": "OK"}"""))

        val resultado = repo.subirAvatar(bytesOk, null, "image/png")

        assertTrue(resultado is Resultado.Exito)
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertTrue(body.contains("filename="))
    }

    @Test
    fun `subirAvatar con 400 VALIDATION_ERROR retorna Validacion`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody("""
                { "timestamp": "2026-08-31", "status": 400, "error": "VALIDATION_ERROR",
                  "message": "Invalid", "path": "/api/v1/users/me/avatar", "details": [{"field":"avatar","message":"Se requiere un archivo."}] }
            """.trimIndent()),
        )
        fakeSesion.guardarToken("valido")

        val resultado = repo.subirAvatar(bytesOk, "foto.png", "image/png")

        assertTrue(resultado is Resultado.Fallo)
        assertTrue((resultado as Resultado.Fallo).error is EraError.Validacion)
        assertTrue(fakeSesion.token != null)
    }

    @Test
    fun `subirAvatar con 401 limpia sesion y retorna SesionExpirada`() = runTest {
        val errorJson = """
            { "timestamp": "2026-08-31", "status": 401, "error": "UNAUTHORIZED",
              "message": "Expirada", "path": "/api/v1/users/me/avatar" }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(401).setBody(errorJson))
        fakeSesion.guardarToken("valido")

        val resultado = repo.subirAvatar(bytesOk, "foto.png", "image/png")

        assertTrue(resultado is Resultado.Fallo)
        assertEquals(EraError.SesionExpirada, (resultado as Resultado.Fallo).error)
        assertTrue(fakeSesion.token == null)
    }

    @Test
    fun `subirAvatar con 403 limpia sesion y retorna CuentaInactiva`() = runTest {
        val errorJson = """
            { "timestamp": "2026-08-31", "status": 403, "error": "ACCOUNT_INACTIVE",
              "message": "Inactiva", "path": "/api/v1/users/me/avatar" }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(403).setBody(errorJson))
        fakeSesion.guardarToken("valido")

        val resultado = repo.subirAvatar(bytesOk, "foto.png", "image/png")

        assertTrue(resultado is Resultado.Fallo)
        assertEquals(EraError.CuentaInactiva, (resultado as Resultado.Fallo).error)
        assertTrue(fakeSesion.token == null)
    }

    @Test
    fun `subirAvatar con 500 retorna ErrorServidor sin limpiar sesion`() = runTest {
        val errorJson = """
            { "timestamp": "2026-08-31", "status": 500, "error": "INTERNAL_ERROR",
              "message": "Error", "path": "/api/v1/users/me/avatar" }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(500).setBody(errorJson))
        fakeSesion.guardarToken("valido")

        val resultado = repo.subirAvatar(bytesOk, "foto.png", "image/png")

        assertTrue(resultado is Resultado.Fallo)
        assertEquals(EraError.ErrorServidor, (resultado as Resultado.Fallo).error)
        assertTrue(fakeSesion.token != null)
    }

    // ---------- obtenerAvatarBytes ----------

    @Test
    fun `obtenerAvatarBytes exito retorna los bytes`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(Buffer().write(bytesOk)))

        val resultado = repo.obtenerAvatarBytes()

        assertTrue(resultado is Resultado.Exito)
        assertTrue((resultado as Resultado.Exito).data.contentEquals(bytesOk))
    }

    @Test
    fun `obtenerAvatarBytes con 404 retorna PerfilNoEncontrado sin limpiar sesion`() = runTest {
        val errorJson = """
            { "timestamp": "2026-08-31", "status": 404, "error": "NOT_FOUND",
              "message": "No", "path": "/api/v1/users/me/avatar" }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(404).setBody(errorJson))
        fakeSesion.guardarToken("valido")

        val resultado = repo.obtenerAvatarBytes()

        assertTrue(resultado is Resultado.Fallo)
        assertEquals(EraError.PerfilNoEncontrado, (resultado as Resultado.Fallo).error)
        assertFalse(fakeSesion.token == null)
    }

    @Test
    fun `obtenerAvatarBytes con 401 limpia sesion`() = runTest {
        val errorJson = """
            { "timestamp": "2026-08-31", "status": 401, "error": "UNAUTHORIZED",
              "message": "Expirada", "path": "/api/v1/users/me/avatar" }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(401).setBody(errorJson))
        fakeSesion.guardarToken("valido")

        val resultado = repo.obtenerAvatarBytes()

        assertTrue(resultado is Resultado.Fallo)
        assertEquals(EraError.SesionExpirada, (resultado as Resultado.Fallo).error)
        assertTrue(fakeSesion.token == null)
    }

    private class FakeSesionRepository : SesionRepository {
        var token: String? = null
        override fun guardarToken(token: String) { this.token = token }
        override fun obtenerToken(): String? = token
        override fun guardarCorreo(correo: String) {}
        override fun obtenerCorreo(): String? = null
        override fun limpiarToken() { token = null }
        override fun tieneToken(): Boolean = token != null
    }
}
