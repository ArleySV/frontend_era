package com.era.app.repository

import android.content.Context
import com.era.app.data.model.FaqItem
import com.era.app.utils.EraError
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
            // Fallo de lectura de un asset local: no es un error de red, así que no
            // pasamos por ErrorMapper.desdeThrowable (mapearía a ErrorConexion).
            Resultado.Fallo(
                EraError.Validacion(listOf("No se pudo cargar las preguntas frecuentes"))
            )
        }
    }
}
