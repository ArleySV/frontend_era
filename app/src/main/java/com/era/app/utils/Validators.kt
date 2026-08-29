package com.era.app.utils

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object Validators {

    const val USERNAME_MIN_LENGTH = 3
    const val USERNAME_MAX_LENGTH = 60
    const val EMAIL_MAX_LENGTH = 255
    const val OTP_LENGTH = 6
    const val CEDULA_UX_MAX_DIGITOS = 15
    const val EDAD_MINIMA = 7
    const val EDAD_MAXIMA = 11

    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val CEDULA_REGEX = Regex("^[A-Za-z0-9]{6,20}$")
    private val FECHA_DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val FECHA_ISO_TO_DISPLAY =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    fun isValidEmail(value: String): Boolean =
        value.length <= EMAIL_MAX_LENGTH && EMAIL_REGEX.matches(value)

    fun isValidNombreUsuario(value: String): Boolean =
        value.length in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH &&
            value.none { it.isWhitespace() }

    fun isValidCedula(value: String): Boolean =
        CEDULA_REGEX.matches(value)

    fun isValidCedulaUx(value: String): Boolean =
        value.isNotEmpty() &&
            value.length <= CEDULA_UX_MAX_DIGITOS &&
            value.all { it in '0'..'9' }

    fun isValidOtp(value: String): Boolean =
        value.length == OTP_LENGTH && value.all { it in '0'..'9' }

    fun parseFechaNacimiento(value: String): LocalDate? =
        try {
            val fecha = LocalDate.parse(value)
            if (fecha.isAfter(LocalDate.now())) null else fecha
        } catch (e: DateTimeParseException) {
            null
        }

    fun parseFechaNacimientoDesdeDisplay(value: String, hoy: LocalDate = LocalDate.now()): LocalDate? {
        val fecha = try {
            LocalDate.parse(value, FECHA_DISPLAY_FORMAT)
        } catch (e: DateTimeParseException) {
            return null
        }
        if (!fecha.isBefore(hoy)) return null
        val edad = Period.between(fecha, hoy).years
        return if (edad in EDAD_MINIMA..EDAD_MAXIMA) fecha else null
    }

    fun formatearFechaISO(iso: String): String? =
        try {
            LocalDate.parse(iso).format(FECHA_ISO_TO_DISPLAY)
        } catch (e: DateTimeParseException) {
            null
        }
}
