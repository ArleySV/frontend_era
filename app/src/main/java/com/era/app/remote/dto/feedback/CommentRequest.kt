package com.era.app.remote.dto.feedback

import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(
    val contenido: String,
)
