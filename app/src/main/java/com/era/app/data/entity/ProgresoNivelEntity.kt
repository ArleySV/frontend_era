package com.era.app.data.entity

import androidx.room.Entity

@Entity(
    tableName = "progreso_niveles",
    primaryKeys = ["userId", "nivelOrden"]
)
data class ProgresoNivelEntity(
    val userId: String,
    val nivelOrden: Int,
    val estadoNivel: String, // BLOQUEADO, DISPONIBLE, COMPLETADO
    val intentosTotales: Int = 0,
    val intentosFallidosConsecutivos: Int = 0,
    val completadoEn: String? = null,
    val sincronizado: Boolean = true
)
