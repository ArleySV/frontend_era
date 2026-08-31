package com.era.app.remote.dto.progress

import kotlinx.serialization.Serializable

@Serializable
data class ResetProgressRequest(
    val contrasena: String,
)
