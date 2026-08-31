package com.era.app.data.entity

/**
 * POJO para capturar el resultado del JOIN entre niveles y progreso.
 */
data class NivelConProgresoEntity(
    val orden: Int,
    val pregunta: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val respuestaCorrecta: Int,
    val estadoNivel: String?, // Null si el usuario no tiene progreso aún en este nivel
    val intentosTotales: Int?,
    val intentosFallidosConsecutivos: Int?,
    val completadoEn: String?,
    val sincronizado: Boolean?
)
