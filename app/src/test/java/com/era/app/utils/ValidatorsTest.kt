package com.era.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    private val hoy = LocalDate.now()
    private val display = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @Test
    fun `email valido basico`() {
        assertTrue(Validators.isValidEmail("acudiente@correo.com"))
    }

    @Test
    fun `email de doscientos cincuenta y cinco pasa y de doscientos sesenta falla`() {
        val local255 = "a".repeat(250) + "@d.io"
        assertEquals(255, local255.length)
        assertTrue(Validators.isValidEmail(local255))

        val email256 = "a".repeat(251) + "@d.io"
        assertEquals(256, email256.length)
        assertFalse(Validators.isValidEmail(email256))
    }

    @Test
    fun `email sin punto en dominio es invalido`() {
        assertFalse(Validators.isValidEmail("acudiente@correo"))
    }

    @Test
    fun `email con espacio es invalido`() {
        assertFalse(Validators.isValidEmail("acu diente@correo.com"))
    }

    @Test
    fun `email con doble arroba es invalido`() {
        assertFalse(Validators.isValidEmail("a@@b.com"))
    }

    @Test
    fun `username en limites tres y sesenta pasa`() {
        assertTrue(Validators.isValidNombreUsuario("abc"))
        assertTrue(Validators.isValidNombreUsuario("u".repeat(60)))
    }

    @Test
    fun `username fuera de limites dos y sesenta y uno falla`() {
        assertFalse(Validators.isValidNombreUsuario("ab"))
        assertFalse(Validators.isValidNombreUsuario("u".repeat(61)))
    }

    @Test
    fun `username con espacio tab o salto de linea es invalido`() {
        assertFalse(Validators.isValidNombreUsuario("usu ario"))
        assertFalse(Validators.isValidNombreUsuario("usuario\ttab"))
        assertFalse(Validators.isValidNombreUsuario("usuario\nsalto"))
    }

    @Test
    fun `cedula seis a veinte alfanumerica`() {
        assertTrue(Validators.isValidCedula("a1b2c3"))
        assertTrue(Validators.isValidCedula("X".repeat(20)))
        assertFalse(Validators.isValidCedula("a1b2c"))
        assertFalse(Validators.isValidCedula("X".repeat(21)))
        assertFalse(Validators.isValidCedula("12345-6"))
    }

    @Test
    fun `otp de seis digitos valida`() {
        assertTrue(Validators.isValidOtp("123456"))
        assertFalse(Validators.isValidOtp("12345"))
        assertFalse(Validators.isValidOtp("1234567"))
        assertFalse(Validators.isValidOtp("12a456"))
        assertFalse(Validators.isValidOtp(""))
    }

    @Test
    fun `otp con digitos unicode se rechaza paridad con regex ascii del backend`() {
        assertFalse(Validators.isValidOtp("١٢٣٤٥٦"))
        assertFalse(Validators.isValidOtp("１２３４５６"))
    }

    @Test
    fun `fecha iso pasada parsea y futura se rechaza`() {
        assertNotNull(Validators.parseFechaNacimiento("2018-05-10"))
        assertNull(Validators.parseFechaNacimiento("2999-01-01"))
        assertNull(Validators.parseFechaNacimiento("10/05/2018"))
        assertNull(Validators.parseFechaNacimiento("2018-13-01"))
        assertNull(Validators.parseFechaNacimiento(""))
    }

    @Test
    fun `cedula ux frontera quince pasa y dieciseis falla`() {
        assertTrue(Validators.isValidCedulaUx("1".repeat(15)))
        assertFalse(Validators.isValidCedulaUx("1".repeat(16)))
    }

    @Test
    fun `cedula ux vacia letras guion o digitos unicode falla`() {
        assertFalse(Validators.isValidCedulaUx(""))
        assertFalse(Validators.isValidCedulaUx("12345678901234a"))
        assertFalse(Validators.isValidCedulaUx("1234-567890123"))
        assertFalse(Validators.isValidCedulaUx("١٢٣٤٥٦٧٨٩٠١٢٣٤٥"))
    }

    @Test
    fun `fecha display edades frontera siete y once pasan`() {
        assertNotNull(Validators.parseFechaNacimientoDesdeDisplay(hoy.minusYears(7).format(display), hoy))
        assertNotNull(Validators.parseFechaNacimientoDesdeDisplay(hoy.minusYears(11).format(display), hoy))
    }

    @Test
    fun `fecha display seis doce futura formato o dia inexistente falla`() {
        assertNull(Validators.parseFechaNacimientoDesdeDisplay(hoy.minusYears(6).format(display), hoy))
        assertNull(Validators.parseFechaNacimientoDesdeDisplay(hoy.minusYears(12).format(display), hoy))
        assertNull(Validators.parseFechaNacimientoDesdeDisplay(hoy.plusDays(1).format(display), hoy))
        assertNull(Validators.parseFechaNacimientoDesdeDisplay("31/02/2016", hoy))
        assertNull(Validators.parseFechaNacimientoDesdeDisplay("10-05-2016", hoy))
        assertNull(Validators.parseFechaNacimientoDesdeDisplay("", hoy))
    }
}
