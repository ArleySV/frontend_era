package com.era.app.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String,
)
