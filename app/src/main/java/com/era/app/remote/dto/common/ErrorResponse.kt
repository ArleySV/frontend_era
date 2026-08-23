package com.era.app.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<FieldError>? = null,
)

@Serializable
data class FieldError(
    val field: String,
    val message: String,
)
