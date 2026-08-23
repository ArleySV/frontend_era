package com.era.app.remote.dto.progress

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelProgress(
    val orden: Int,
    @SerialName("estado_nivel")
    val estadoNivel: String,
    @SerialName("intentos_totales")
    val intentosTotales: Int,
    @SerialName("completado_en")
    val completadoEn: String? = null,
    @SerialName("ultima_interaccion")
    val ultimaInteraccion: String,
)
