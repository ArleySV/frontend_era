package com.era.app.ui.faq

import com.era.app.data.model.FaqItem
import com.era.app.utils.EraError

data class FaqUiState(
    val faqs: List<FaqItem> = emptyList(),
    val cargandoFaqs: Boolean = false,
    val comentario: String = "",
    val enviandoComentario: Boolean = false,
    val errorComentario: EraError? = null,
    val errorFaqs: EraError? = null
) {
    val puedeEnviarComentario: Boolean 
        get() = comentario.isNotBlank() && comentario.length <= 2000 && !enviandoComentario
    
    val longitudComentario: Int get() = comentario.length
}

sealed interface FaqEvento {
    data object ComentarioEnviado : FaqEvento
    data class Error(val error: EraError) : FaqEvento
    data object SesionExpirada : FaqEvento
}
