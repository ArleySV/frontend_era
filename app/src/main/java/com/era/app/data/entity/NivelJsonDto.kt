package com.era.app.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class NivelJsonDto(
    val orden: Int,
    val pregunta: String,
    val opciones: List<String>,
    val respuestaCorrecta: Int
)
