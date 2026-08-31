package com.era.app.repository

import com.era.app.data.model.FaqItem

interface FaqRepository {
    /**
     * Retorna la lista de preguntas frecuentes cargadas desde el recurso local.
     */
    suspend fun obtenerFaqs(): Resultado<List<FaqItem>>
}
