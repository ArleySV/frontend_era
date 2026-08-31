package com.era.app.repository

import com.era.app.remote.api.ProgressApi
import com.era.app.remote.dto.common.ErrorResponse
import com.era.app.remote.dto.progress.LevelProgress
import com.era.app.remote.dto.progress.ProgressSyncItem
import com.era.app.remote.dto.progress.ProgressSyncRequest
import com.era.app.remote.dto.progress.ResetProgressRequest
import com.era.app.data.dao.ProgresoDao
import com.era.app.data.entity.ProgresoNivelEntity
import com.era.app.data.model.NivelConProgreso
import com.era.app.utils.EraError
import com.era.app.utils.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomProgresoRepository @Inject constructor(
    private val api: ProgressApi,
    private val progresoDao: ProgresoDao,
    private val sesionRepository: SesionRepository,
    private val json: Json,
) : ProgresoRepository {

    override fun obtenerNivelesConProgreso(): Flow<List<NivelConProgreso>> {
        val correo = sesionRepository.obtenerCorreo() ?: return flowOf(emptyList())
        return progresoDao.obtenerTodoConProgreso(correo).map { lista ->
            lista.map { entity ->
                NivelConProgreso(
                    orden = entity.orden,
                    pregunta = entity.pregunta,
                    opcionA = entity.opcionA,
                    opcionB = entity.opcionB,
                    opcionC = entity.opcionC,
                    respuestaCorrecta = entity.respuestaCorrecta,
                    estado = entity.estadoNivel ?: if (entity.orden == 1) "DISPONIBLE" else "BLOQUEADO",
                    intentosTotales = entity.intentosTotales ?: 0,
                    intentosFallidosConsecutivos = entity.intentosFallidosConsecutivos ?: 0,
                    completadoEn = entity.completadoEn,
                    sincronizado = entity.sincronizado ?: true
                )
            }
        }
    }

    override suspend fun registrarResultado(orden: Int, exito: Boolean): Resultado<Unit> {
        val correo = sesionRepository.obtenerCorreo() ?: return Resultado.Fallo(EraError.SesionExpirada)
        
        val actual = progresoDao.obtenerPorNivel(correo, orden)
        
        // El estado solo avanza: BLOQUEADO -> DISPONIBLE -> COMPLETADO
        val estadoActual = actual?.estadoNivel ?: if (orden == 1) "DISPONIBLE" else "BLOQUEADO"
        val nuevoEstado = if (exito) {
            "COMPLETADO"
        } else {
            estadoActual
        }
        
        // 1. Registrar resultado del nivel actual
        val nuevoProgreso = ProgresoNivelEntity(
            userId = correo,
            nivelOrden = orden,
            estadoNivel = nuevoEstado,
            intentosTotales = (actual?.intentosTotales ?: 0) + 1,
            intentosFallidosConsecutivos = if (exito) 0 else (actual?.intentosFallidosConsecutivos ?: 0) + 1,
            completadoEn = actual?.completadoEn, // El servidor es autoridad para esta fecha
            sincronizado = false
        )
        progresoDao.insertarOActualizar(nuevoProgreso)

        // 2. Desbloquear el siguiente nivel si corresponde
        if (exito) {
            val siguienteOrden = orden + 1
            if (siguienteOrden <= 20) {
                val actualSiguiente = progresoDao.obtenerPorNivel(correo, siguienteOrden)
                if (actualSiguiente == null || actualSiguiente.estadoNivel == "BLOQUEADO") {
                    progresoDao.insertarOActualizar(ProgresoNivelEntity(
                        userId = correo,
                        nivelOrden = siguienteOrden,
                        estadoNivel = "DISPONIBLE",
                        sincronizado = false
                    ))
                }
            }
        }

        return Resultado.Exito(Unit)
    }

    override suspend fun sincronizarConServidor(): Resultado<Unit> {
        val correo = sesionRepository.obtenerCorreo() ?: return Resultado.Fallo(EraError.SesionExpirada)

        // 1. Obtener cambios locales pendientes
        val pendientes = progresoDao.obtenerPendientesDeSincronizar(correo)

        val resultado = if (pendientes.isEmpty()) {
            // Si no hay cambios, solo refrescamos el estado desde el servidor
            llamar { api.getProgress() }
        } else {
            // Si hay cambios, los subimos (POST /sync mergea y devuelve el snapshot final)
            val request = ProgressSyncRequest(
                progreso = pendientes.map {
                    ProgressSyncItem(
                        orden = it.nivelOrden,
                        estadoNivel = it.estadoNivel,
                        intentosTotales = it.intentosTotales,
                        intentosFallidosConsecutivos = it.intentosFallidosConsecutivos
                    )
                }
            )
            llamar { api.syncProgress(request) }
        }

        return when (resultado) {
            is Resultado.Exito -> {
                val snapshot = resultado.data.progreso
                mergeSnapshotRemoto(correo, snapshot)
                Resultado.Exito(Unit)
            }
            is Resultado.Fallo -> {
                if (resultado.error is EraError.SesionExpirada || resultado.error is EraError.CuentaInactiva) {
                    sesionRepository.limpiarToken()
                }
                Resultado.Fallo(resultado.error)
            }
        }
    }

    override suspend fun reiniciarProgreso(contrasena: String): Resultado<Unit> {
        val correo = sesionRepository.obtenerCorreo() ?: return Resultado.Fallo(EraError.SesionExpirada)
        
        // POST /reset mergea el reset en el servidor y devuelve el snapshot (Nivel 1 disponible)
        val resultado = llamar { api.resetProgress(ResetProgressRequest(contrasena)) }
        
        return when (resultado) {
            is Resultado.Exito -> {
                // Limpiar localmente y aplicar snapshot del servidor
                progresoDao.borrarTodoParaUsuario(correo)
                mergeSnapshotRemoto(correo, resultado.data.progreso)
                Resultado.Exito(Unit)
            }
            is Resultado.Fallo -> {
                if (resultado.error is EraError.SesionExpirada || resultado.error is EraError.CuentaInactiva) {
                    sesionRepository.limpiarToken()
                }
                Resultado.Fallo(resultado.error)
            }
        }
    }

    private suspend fun mergeSnapshotRemoto(userId: String, snapshot: List<LevelProgress>) {
        val progresoLocal = snapshot.map { remoto ->
            val local = progresoDao.obtenerPorNivel(userId, remoto.orden)
            
            val stateLocal = local?.estadoNivel ?: "BLOQUEADO"
            val stateRemoto = remoto.estadoNivel
            
            // Regla: servidor es autoridad pero no permitimos retrocesos si local es mayor (aunque el servidor ya hace max)
            val finalState = if (prioridadEstado(stateRemoto) >= prioridadEstado(stateLocal)) stateRemoto else stateLocal
            val finalIntentos = maxOf(local?.intentosTotales ?: 0, remoto.intentosTotales)
            
            // Se marca sincronizado=true solo si el servidor tiene lo mismo o algo mejor que lo que enviamos
            // (En el POST /sync el servidor devuelve el snapshot tras su propio merge)
            val isSynced = prioridadEstado(stateRemoto) >= prioridadEstado(stateLocal) && 
                           remoto.intentosTotales >= (local?.intentosTotales ?: 0)

            ProgresoNivelEntity(
                userId = userId,
                nivelOrden = remoto.orden,
                estadoNivel = finalState,
                intentosTotales = finalIntentos,
                intentosFallidosConsecutivos = local?.intentosFallidosConsecutivos ?: 0, // Fallidos consecutivos son locales/temporales
                completadoEn = remoto.completadoEn ?: local?.completadoEn,
                sincronizado = isSynced
            )
        }
        progresoDao.insertarLote(progresoLocal)
    }

    private fun prioridadEstado(estado: String): Int = when (estado) {
        "COMPLETADO" -> 2
        "DISPONIBLE" -> 1
        else -> 0
    }

    private suspend fun <T> llamar(bloque: suspend () -> T): Resultado<T> =
        try {
            Resultado.Exito(bloque())
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            Resultado.Fallo(aEraError(t))
        }

    private fun aEraError(t: Throwable): EraError = when (t) {
        is HttpException -> {
            val body = t.response()?.errorBody()?.string()
                ?.let { raw -> runCatching { json.decodeFromString<ErrorResponse>(raw) }.getOrNull() }
            ErrorMapper.desdeHttp(t.code(), body)
        }
        else -> ErrorMapper.desdeThrowable(t)
    }
}
