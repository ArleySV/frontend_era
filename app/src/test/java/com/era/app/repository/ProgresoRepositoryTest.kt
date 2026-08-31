package com.era.app.repository

import com.era.app.data.dao.ProgresoDao
import com.era.app.data.entity.NivelConProgresoEntity
import com.era.app.data.entity.ProgresoNivelEntity
import com.era.app.remote.api.ProgressApi
import com.era.app.remote.dto.progress.LevelProgress
import com.era.app.remote.dto.progress.ProgressSummary
import com.era.app.remote.dto.progress.ProgressSyncRequest
import com.era.app.remote.dto.progress.ProgressSyncResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgresoRepositoryTest {

    private lateinit var repo: RoomProgresoRepository
    private lateinit var fakeProgresoDao: FakeProgresoDao
    private lateinit var fakeSesion: FakeSesionRepository
    private lateinit var fakeApi: FakeProgressApi

    @Before
    fun setUp() {
        fakeProgresoDao = FakeProgresoDao()
        fakeSesion = FakeSesionRepository()
        fakeApi = FakeProgressApi()
        repo = RoomProgresoRepository(
            api = fakeApi,
            progresoDao = fakeProgresoDao,
            sesionRepository = fakeSesion,
            json = Json { ignoreUnknownKeys = true }
        )
    }

    @Test
    fun `obtenerNivelesConProgreso con usuario nuevo retorna nivel 1 disponible`() = runTest {
        fakeSesion.guardarCorreo("test@test.com")
        fakeProgresoDao.respuestaJoin = listOf(
            nivelEntity(1),
            nivelEntity(2)
        )

        val resultado = repo.obtenerNivelesConProgreso().first()

        assertEquals(2, resultado.size)
        assertEquals("DISPONIBLE", resultado[0].estado)
        assertEquals("BLOQUEADO", resultado[1].estado)
    }

    @Test
    fun `registrarResultado exito marca nivel como completado y desbloquea el siguiente`() = runTest {
        val correo = "test@test.com"
        fakeSesion.guardarCorreo(correo)
        
        repo.registrarResultado(1, true)

        val inserts = fakeProgresoDao.registrosInsertados
        assertEquals(2, inserts.size)
        
        assertEquals(1, inserts[0].nivelOrden)
        assertEquals("COMPLETADO", inserts[0].estadoNivel)
        assertFalse(inserts[0].sincronizado)
        
        assertEquals(2, inserts[1].nivelOrden)
        assertEquals("DISPONIBLE", inserts[1].estadoNivel)
    }

    @Test
    fun `registrarResultado fallo incrementa intentos y no desbloquea el siguiente`() = runTest {
        fakeSesion.guardarCorreo("test@test.com")
        fakeProgresoDao.mapaProgreso[1] = ProgresoNivelEntity("test@test.com", 1, "DISPONIBLE", intentosTotales = 1)

        repo.registrarResultado(1, false)

        val inserts = fakeProgresoDao.registrosInsertados
        assertEquals(1, inserts.size)
        assertEquals(1, inserts[0].nivelOrden)
        assertEquals("DISPONIBLE", inserts[0].estadoNivel)
        assertEquals(2, inserts[0].intentosTotales)
        assertEquals(1, inserts[0].intentosFallidosConsecutivos)
    }

    @Test
    fun `sincronizarConServidor sube pendientes y mergea respuesta`() = runTest {
        val correo = "test@test.com"
        fakeSesion.guardarCorreo(correo)
        
        // Simular un cambio local pendiente (Nivel 1 completado offline)
        val localDirty = ProgresoNivelEntity(correo, 1, "COMPLETADO", intentosTotales = 2, sincronizado = false)
        fakeProgresoDao.pendientes = listOf(localDirty)
        fakeProgresoDao.mapaProgreso[1] = localDirty

        // Simular respuesta del servidor (Nivel 1 completado con 3 intentos en otro dispositivo)
        fakeApi.syncProgressResponse = ProgressSyncResponse(
            progreso = listOf(
                LevelProgress(orden = 1, estadoNivel = "COMPLETADO", intentosTotales = 3, ultimaInteraccion = "2026-08-30T10:00:00Z")
            ),
            resumen = ProgressSummary(nivelesCompletados = 1, totalNiveles = 20, totalReintentos = 3)
        )

        val r = repo.sincronizarConServidor()

        assertTrue(r is Resultado.Exito)
        assertEquals(1, fakeApi.lastSyncRequest?.progreso?.size)
        assertEquals(1, fakeProgresoDao.lotesInsertados.size)
        val insertado = fakeProgresoDao.lotesInsertados.first()[0]
        assertEquals(1, insertado.nivelOrden)
        assertEquals(3, insertado.intentosTotales)
        assertTrue(insertado.sincronizado)
    }

    private fun nivelEntity(orden: Int) = NivelConProgresoEntity(
        orden = orden,
        pregunta = "P$orden",
        opcionA = "A", opcionB = "B", opcionC = "C",
        respuestaCorrecta = 0,
        estadoNivel = null,
        intentosTotales = null,
        intentosFallidosConsecutivos = null,
        completadoEn = null,
        sincronizado = null
    )

    private class FakeProgressApi : ProgressApi {
        var getProgressResponse: ProgressSyncResponse? = null
        var syncProgressResponse: ProgressSyncResponse? = null
        var lastSyncRequest: ProgressSyncRequest? = null
        var resetProgressResponse: ProgressSyncResponse? = null

        override suspend fun getProgress(): ProgressSyncResponse = getProgressResponse!!
        override suspend fun syncProgress(request: ProgressSyncRequest): ProgressSyncResponse {
            lastSyncRequest = request
            return syncProgressResponse!!
        }
        override suspend fun resetProgress(request: com.era.app.remote.dto.progress.ResetProgressRequest): ProgressSyncResponse = resetProgressResponse!!
    }

    private class FakeProgresoDao : ProgresoDao {
        val registrosInsertados = mutableListOf<ProgresoNivelEntity>()
        val lotesInsertados = mutableListOf<List<ProgresoNivelEntity>>()
        var respuestaJoin = listOf<NivelConProgresoEntity>()
        val mapaProgreso = mutableMapOf<Int, ProgresoNivelEntity>()
        var pendientes = listOf<ProgresoNivelEntity>()

        override fun obtenerProgresoPorUsuario(userId: String): Flow<List<ProgresoNivelEntity>> = flowOf(emptyList())
        override suspend fun obtenerPorNivel(userId: String, orden: Int): ProgresoNivelEntity? = mapaProgreso[orden]
        override suspend fun insertarOActualizar(progreso: ProgresoNivelEntity) { registrosInsertados.add(progreso) }
        override suspend fun insertarLote(progreso: List<ProgresoNivelEntity>) { lotesInsertados.add(progreso) }
        override suspend fun obtenerPendientesDeSincronizar(userId: String): List<ProgresoNivelEntity> = pendientes
        override suspend fun marcarComoSincronizados(userId: String, ordenes: List<Int>) {}
        override suspend fun borrarTodoParaUsuario(userId: String) {}
        override fun obtenerTodoConProgreso(userId: String): Flow<List<NivelConProgresoEntity>> = flowOf(respuestaJoin)
    }

    private class FakeSesionRepository : SesionRepository {
        var token: String? = null
        var correo: String? = null
        override fun guardarToken(token: String) { this.token = token }
        override fun obtenerToken(): String? = token
        override fun guardarCorreo(correo: String) { this.correo = correo }
        override fun obtenerCorreo(): String? = correo
        override fun limpiarToken() { token = null; correo = null }
        override fun tieneToken(): Boolean = token != null
    }
}
