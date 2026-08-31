package com.era.app.data.model

data class NivelConProgreso(
    val orden: Int,
    val pregunta: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val respuestaCorrecta: Int,
    val estado: String, // BLOQUEADO, DISPONIBLE, COMPLETADO
    val intentosTotales: Int,
    val intentosFallidosConsecutivos: Int,
    val completadoEn: String?,
    val sincronizado: Boolean
)
