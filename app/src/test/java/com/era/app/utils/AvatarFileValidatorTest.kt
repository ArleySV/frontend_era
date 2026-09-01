package com.era.app.utils

import com.era.app.repository.Resultado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarFileValidatorTest {

    @Test
    fun `acepta jpeg de tamanio valido`() {
        val resultado = AvatarFileValidator.validar(byteArrayOf(1, 2, 3, 4), "foto.jpg", "image/jpeg")

        assertTrue(resultado is Resultado.Exito)
        assertEquals("image/jpeg", (resultado as Resultado.Exito).data.mimeType)
    }

    @Test
    fun `acepta png y webp`() {
        assertTrue(AvatarFileValidator.validar(byteArrayOf(1, 2), "a.png", "image/png") is Resultado.Exito)
        assertTrue(AvatarFileValidator.validar(byteArrayOf(1, 2), "a.webp", "image/webp") is Resultado.Exito)
    }

    @Test
    fun `rechaza mime no permitido gif`() {
        val resultado = AvatarFileValidator.validar(byteArrayOf(1, 2), "a.gif", "image/gif")

        assertTrue(resultado is Resultado.Fallo)
        val error = (resultado as Resultado.Fallo).error as EraError.Validacion
        assertTrue(error.detalles.contains("Formato no soportado"))
    }

    @Test
    fun `rechaza mime nulo o vacio`() {
        assertTrue(AvatarFileValidator.validar(byteArrayOf(1), "a", null) is Resultado.Fallo)
        assertTrue(AvatarFileValidator.validar(byteArrayOf(1), "a", "") is Resultado.Fallo)
    }

    @Test
    fun `rechaza archivo vacio`() {
        val resultado = AvatarFileValidator.validar(ByteArray(0), "a.png", "image/png")

        assertTrue(resultado is Resultado.Fallo)
        assertTrue(((resultado as Resultado.Fallo).error as EraError.Validacion).detalles.contains("El archivo está vacío"))
    }

    @Test
    fun `rechaza archivo mayor a 2 MB`() {
        val bytes = ByteArray((AvatarFileValidator.MAX_BYTES_AVATAR + 1).toInt())
        val resultado = AvatarFileValidator.validar(bytes, "a.png", "image/png")

        assertTrue(resultado is Resultado.Fallo)
        assertTrue(((resultado as Resultado.Fallo).error as EraError.Validacion).detalles.contains("La imagen no puede superar 2 MB"))
    }

    @Test
    fun `acepta exactamente 2 MB`() {
        val bytes = ByteArray(AvatarFileValidator.MAX_BYTES_AVATAR.toInt())
        val resultado = AvatarFileValidator.validar(bytes, "a.png", "image/png")

        assertTrue(resultado is Resultado.Exito)
    }

    @Test
    fun `conserva filename y bytes en exito`() {
        val bytes = byteArrayOf(7, 8, 9)
        val resultado = AvatarFileValidator.validar(bytes, "selfie.png", "image/png") as Resultado.Exito

        assertEquals("selfie.png", resultado.data.filename)
        assertTrue(resultado.data.bytes.contentEquals(bytes))
    }

    @Test
    fun `sobrecarga por tamano rechaza archivo mayor a 2 MB`() {
        val resultado = AvatarFileValidator.validar(AvatarFileValidator.MAX_BYTES_AVATAR + 1, "image/png")

        assertTrue(resultado is Resultado.Fallo)
        assertTrue(((resultado as Resultado.Fallo).error as EraError.Validacion).detalles.contains("La imagen no puede superar 2 MB"))
    }

    @Test
    fun `sobrecarga por tamano rechaza mime no permitido`() {
        val resultado = AvatarFileValidator.validar(1024L, "image/gif")

        assertTrue(resultado is Resultado.Fallo)
        assertTrue(((resultado as Resultado.Fallo).error as EraError.Validacion).detalles.contains("Formato no soportado"))
    }

    @Test
    fun `sobrecarga por tamano acepta dentro del limite`() {
        val resultado = AvatarFileValidator.validar(1024L, "image/png")

        assertTrue(resultado is Resultado.Exito)
        assertTrue((resultado as Resultado.Exito).data.bytes.isEmpty())
    }
}
