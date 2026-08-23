package com.era.app.remote.dto.progress

import kotlinx.serialization.Serializable

@Serializable
data class ProgressSyncResponse(
    val progreso: List<LevelProgress>,
    val resumen: ProgressSummary,
)
