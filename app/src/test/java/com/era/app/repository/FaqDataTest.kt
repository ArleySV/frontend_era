package com.era.app.repository

import com.era.app.data.model.FaqItem
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FaqDataTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `validar integridad de faq_json`() {
        // En entorno de ejecución de tests unitarios de Gradle, 
        // el directorio de trabajo es el módulo (app)
        val faqFile = File("src/main/assets/faq.json")
        assertTrue("El archivo faq.json no existe en assets", faqFile.exists())

        val jsonString = faqFile.readText()
        val faqs = json.decodeFromString<List<FaqItem>>(jsonString)

        // 1. Validar que existan las 8 preguntas oficiales
        assertEquals("Deben existir exactamente 8 preguntas frecuentes", 8, faqs.size)

        // 2. Validar IDs únicos, consecutivos y ordenados
        faqs.forEachIndexed { index, faq ->
            val expectedId = index + 1
            assertEquals("ID incorrecto en posición $index", expectedId, faq.id)
            
            // 3. Validar contenido no vacío
            assertTrue("La pregunta del ID ${faq.id} está vacía", faq.pregunta.isNotBlank())
            assertTrue("La respuesta del ID ${faq.id} está vacía", faq.respuesta.isNotBlank())
        }
    }

    @Test
    fun `deserializacion de FaqItem funciona correctamente`() {
        val sample = """{"id":1, "pregunta":"P1", "respuesta":"R1"}"""
        val item = json.decodeFromString<FaqItem>(sample)
        assertEquals(1, item.id)
        assertEquals("P1", item.pregunta)
        assertEquals("R1", item.respuesta)
    }
}
