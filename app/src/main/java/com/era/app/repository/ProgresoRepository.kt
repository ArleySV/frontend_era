package com.era.app.repository

import com.era.app.data.model.NivelConProgreso
import kotlinx.coroutines.flow.Flow

interface ProgresoRepository {
    /**
     * Retorna el flujo de todos los niveles del catálogo con su respectivo progreso
     * para el usuario actual.
     */
    fun obtenerNivelesConProgreso(): Flow<List<NivelConProgreso>>

    /**
     * Registra el resultado de un nivel jugado localmente.
     * Marca el registro como no sincronizado.
     */
    suspend fun registrarResultado(orden: Int, exito: Boolean): Resultado<Unit>

    /**
     * Fuerza la sincronización manual con el servidor.
     */
    suspend fun sincronizarConServidor(): Resultado<Unit>

    /**
     * Reinicia el progreso del usuario local y remotamente.
     */
    suspend fun reiniciarProgreso(contrasena: String): Resultado<Unit>
}
