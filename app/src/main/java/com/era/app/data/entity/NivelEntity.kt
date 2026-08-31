package com.era.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "niveles")
data class NivelEntity(
    @PrimaryKey val orden: Int,
    val pregunta: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val respuestaCorrecta: Int
)
