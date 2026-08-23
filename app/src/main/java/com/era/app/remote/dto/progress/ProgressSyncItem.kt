package com.era.app.remote.dto.progress

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgressSyncItem(
    val orden: Int,
    @SerialName("estado_nivel")
    val estadoNivel: String,
    @SerialName("intentos_totales")
    val intentosTotales: Int,
    @SerialName("intentos_fallidos_consecutivos")
    val intentosFallidosConsecutivos: Int,
)
