package com.era.app.repository

import com.era.app.remote.api.FeedbackApi
import com.era.app.utils.EraError
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FeedbackRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: RemoteFeedbackRepository
    private lateinit var fakeSesion: FakeSesionRepository
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(FeedbackApi::class.java)
        fakeSesion = FakeSesionRepository()
        repo = RemoteFeedbackRepository(api, fakeSesion, json)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `enviarComentario exito retorna Unit`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"message": "OK"}"""))

        val resultado = repo.enviarComentario("Me gusta")

        assertTrue(resultado is Resultado.Exito)
    }

    @Test
    fun `enviarComentario con 401 limpia sesion y retorna SesionExpirada`() = runTest {
        val errorJson = """
            {
                "timestamp": "2026-08-30",
                "status": 401,
                "error": "UNAUTHORIZED",
                "message": "Expulado",
                "path": "/api/v1/feedback/comments"
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(401).setBody(errorJson))
        fakeSesion.guardarToken("valido")

        val resultado = repo.enviarComentario("Test")

        assertTrue(resultado is Resultado.Fallo)
        assertEquals(EraError.SesionExpirada, (resultado as Resultado.Fallo).error)
        assertTrue(fakeSesion.token == null)
    }

    @Test
    fun `enviarComentario con texto muy largo falla localmente`() = runTest {
        val largo = "a".repeat(2001)
        val resultado = repo.enviarComentario(largo)

        assertTrue(resultado is Resultado.Fallo)
        assertTrue((resultado as Resultado.Fallo).error is EraError.Validacion)
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
