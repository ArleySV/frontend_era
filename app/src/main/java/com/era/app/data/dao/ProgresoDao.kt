package com.era.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.era.app.data.entity.NivelConProgresoEntity
import com.era.app.data.entity.ProgresoNivelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgresoDao {
    @Query("SELECT * FROM progreso_niveles WHERE userId = :userId ORDER BY nivelOrden ASC")
    fun obtenerProgresoPorUsuario(userId: String): Flow<List<ProgresoNivelEntity>>

    @Query("SELECT * FROM progreso_niveles WHERE userId = :userId AND nivelOrden = :orden")
    suspend fun obtenerPorNivel(userId: String, orden: Int): ProgresoNivelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(progreso: ProgresoNivelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLote(progreso: List<ProgresoNivelEntity>)

    @Query("SELECT * FROM progreso_niveles WHERE sincronizado = 0 AND userId = :userId")
    suspend fun obtenerPendientesDeSincronizar(userId: String): List<ProgresoNivelEntity>

    @Query("UPDATE progreso_niveles SET sincronizado = 1 WHERE userId = :userId AND nivelOrden IN (:ordenes)")
    suspend fun marcarComoSincronizados(userId: String, ordenes: List<Int>)

    @Query("DELETE FROM progreso_niveles WHERE userId = :userId")
    suspend fun borrarTodoParaUsuario(userId: String)

    @Transaction
    @Query("""
        SELECT n.orden, n.pregunta, n.opcionA, n.opcionB, n.opcionC, n.respuestaCorrecta,
               p.estadoNivel, p.intentosTotales, p.intentosFallidosConsecutivos, p.completadoEn, p.sincronizado
        FROM niveles n
        LEFT JOIN progreso_niveles p ON n.orden = p.nivelOrden AND p.userId = :userId
        ORDER BY n.orden ASC
    """)
    fun obtenerTodoConProgreso(userId: String): Flow<List<NivelConProgresoEntity>>
}
