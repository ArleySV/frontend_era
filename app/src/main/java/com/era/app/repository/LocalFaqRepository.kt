package com.era.app.repository

import android.content.Context
import com.era.app.data.model.FaqItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFaqRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) : FaqRepository {

    override suspend fun obtenerFaqs(): Resultado<List<FaqItem>> {
        return try {
            val faqJson = context.assets.open("faq.json")
                .bufferedReader().use { it.readText() }
            val faqs = json.decodeFromString<List<FaqItem>>(faqJson)
            Resultado.Exito(faqs)
        } catch (e: Exception) {
            // Reutilizamos el mapeo de errores del proyecto para consistencia
            Resultado.Fallo(com.era.app.utils.ErrorMapper.desdeThrowable(e))
        }
    }
}
