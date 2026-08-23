package com.era.app.remote.dto.progress

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgressSummary(
    @SerialName("niveles_completados")
    val nivelesCompletados: Int,
    @SerialName("total_niveles")
    val totalNiveles: Int,
    @SerialName("total_reintentos")
    val totalReintentos: Int,
)
