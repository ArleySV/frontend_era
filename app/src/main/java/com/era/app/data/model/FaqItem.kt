package com.era.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FaqItem(
    val id: Int,
    val pregunta: String,
    val respuesta: String
)
