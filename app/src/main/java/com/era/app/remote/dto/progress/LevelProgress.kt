package com.era.app.remote.dto.progress

import kotlinx.serialization.Serializable

@Serializable
data class LevelProgress(
    val orden: Int,
    val estadoNivel: String,
    val intentosTotales: Int,
    val completadoEn: String? = null,
    val ultimaInteraccion: String,
)
