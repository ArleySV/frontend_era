package com.era.app.remote.dto.progress

import kotlinx.serialization.Serializable

@Serializable
data class ProgressSummary(
    val nivelesCompletados: Int,
    val totalNiveles: Int,
    val totalReintentos: Int,
)
