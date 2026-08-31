package com.era.app.repository

interface FeedbackRepository {
    /**
     * Envía un comentario o sugerencia al servidor.
     */
    suspend fun enviarComentario(contenido: String): Resultado<Unit>
}
