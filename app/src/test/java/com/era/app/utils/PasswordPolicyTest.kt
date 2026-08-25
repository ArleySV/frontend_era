package com.era.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {

    private val usuario = "acudiente01"
    private val menor = "Mateo Torres"

    @Test
    fun `siete caracteres falla y ocho pasa`() {
        assertTrue(
            PasswordPolicy.errores("Ab1!Ab1", usuario, menor)
                .any { it.contains("8") }
        )
        assertTrue(PasswordPolicy.esValida("Ab1!Ab1!", usuario, menor))
    }

    @Test
    fun `sin mayuscula falla solo por ese criterio`() {
        val errores = PasswordPolicy.errores("abcdef1!", usuario, menor)
        assertEquals(1, errores.size)
        assertTrue(errores[0].contains("mayúscula"))
    }

    @Test
    fun `sin minuscula falla solo por ese criterio`() {
        val errores = PasswordPolicy.errores("ABCDEF1!", usuario, menor)
        assertEquals(1, errores.size)
        assertTrue(errores[0].contains("minúscula"))
    }

    @Test
    fun `sin numero falla solo por ese criterio`() {
        val errores = PasswordPolicy.errores("Abcdefg!", usuario, menor)
        assertEquals(1, errores.size)
        assertTrue(errores[0].contains("número"))
    }

    @Test
    fun `sin simbolo falla solo por ese criterio`() {
        val errores = PasswordPolicy.errores("Abcdef12", usuario, menor)
        assertEquals(1, errores.size)
        assertTrue(errores[0].contains("símbolo"))
    }

    @Test
    fun `espacio no cuenta como simbolo`() {
        val errores = PasswordPolicy.errores("Abcd ef12", usuario, menor)
        assertTrue(errores.any { it.contains("símbolo") })
    }

    @Test
    fun `setenta y dos pasa y setenta y tres falla`() {
        val exacta = "Aa1!".repeat(18)
        assertEquals(72, exacta.length)
        assertTrue(PasswordPolicy.esValida(exacta, usuario, menor))

        val excedida = "$exacta!"
        assertEquals(73, excedida.length)
        assertTrue(
            PasswordPolicy.errores(excedida, usuario, menor)
                .any { it.contains("72") }
        )
    }

    @Test
    fun `igualdad con username en distinta capitalizacion bloquea aunque cumpla complejidad`() {
        val username = "AbcdEf1!"
        val password = "abcdEF1!"
        val errores = PasswordPolicy.errores(password, username, menor)
        assertTrue(errores.any { it.contains("nombre de usuario") })
        assertEquals(1, errores.size)
    }

    @Test
    fun `token de nombre menor a tres caracteres no bloquea`() {
        assertTrue(
            PasswordPolicy.esValida("Mario7#Luz", usuario, "Lu y Yo")
        )
    }

    @Test
    fun `token de nombre de tres caracteres si bloquea`() {
        val errores = PasswordPolicy.errores("Ana1234!", usuario, "Ana")
        assertTrue(errores.any { it.contains("datos personales") })
    }

    @Test
    fun `token del nombre bloquea ignorando mayusculas y acentos de posicion`() {
        val errores = PasswordPolicy.errores("xTorres9!", usuario, "Mateo torres")
        assertTrue(errores.any { it.contains("datos personales") })
    }

    @Test
    fun `simbolos fuera del set basico son validos paridad con backend`() {
        assertTrue(PasswordPolicy.esValida("Abcdef1€?", usuario, menor))
        assertTrue(PasswordPolicy.esValida("Abcdef1~ñ<", usuario, menor))
        assertTrue(PasswordPolicy.esValida("Abcdef1\\/", usuario, menor))
    }

    @Test
    fun `mayuscula unicode cuenta como mayuscula semantica jvm`() {
        val errores = PasswordPolicy.errores("Íbcdef1!", usuario, menor)
        assertFalse(errores.any { it.contains("mayúscula") })
    }

    @Test
    fun `criterios marca todo cumplido en contrasena fuerte`() {
        val c = PasswordPolicy.criterios("ClaveSegura1!", usuario, menor)
        assertTrue(c.longitudMinima)
        assertTrue(c.tieneMayuscula)
        assertTrue(c.tieneMinuscula)
        assertTrue(c.tieneNumero)
        assertTrue(c.tieneSimbolo)
        assertTrue(c.distintaDeUsuario)
        assertTrue(c.sinDatosPersonales)
    }

    @Test
    fun `criterios refleja cada regla incumplida por separado`() {
        val debil = PasswordPolicy.criterios("abcdefg", usuario, menor)
        assertFalse(debil.longitudMinima)
        assertFalse(debil.tieneMayuscula)
        assertTrue(debil.tieneMinuscula)
        assertFalse(debil.tieneNumero)
        assertFalse(debil.tieneSimbolo)

        assertFalse(
            PasswordPolicy.criterios("Abcdef1!", "abcDEF1!", menor).distintaDeUsuario
        )
        assertFalse(
            PasswordPolicy.criterios("Ab1!Mateo", usuario, "Mateo Torres").sinDatosPersonales
        )
        assertTrue(
            PasswordPolicy.criterios("Mario7#Luz", usuario, "Lu y Yo").sinDatosPersonales
        )
    }

    @Test
    fun `criterios y errores son coherentes entre si en muestras variadas`() {
        listOf(
            "",
            "abcdefgh",
            "ABCDEFGH1!",
            "Abcdef12",
            "Abcd ef1!",
            "MateoTorres1!",
            "xTorres1!A",
            "acudiente01",
        ).forEach { clave ->
            val c = PasswordPolicy.criterios(clave, usuario, menor)
            val errores = PasswordPolicy.errores(clave, usuario, menor)
            assertEquals(!c.longitudMinima, errores.any { it.contains("8") })
            assertEquals(!c.tieneMayuscula, errores.any { it.contains("mayúscula") })
            assertEquals(!c.tieneMinuscula, errores.any { it.contains("minúscula") })
            assertEquals(!c.tieneNumero, errores.any { it.contains("número") })
            assertEquals(!c.tieneSimbolo, errores.any { it.contains("símbolo") })
            assertEquals(!c.distintaDeUsuario, errores.any { it.contains("nombre de usuario") })
            assertEquals(!c.sinDatosPersonales, errores.any { it.contains("datos personales") })
        }
    }
}
