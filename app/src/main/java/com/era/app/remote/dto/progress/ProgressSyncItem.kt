package com.era.app.remote.dto.progress

import kotlinx.serialization.Serializable

@Serializable
data class ProgressSyncItem(
    val orden: Int,
    val estadoNivel: String,
    val intentosTotales: Int,
    val intentosFallidosConsecutivos: Int,
)
