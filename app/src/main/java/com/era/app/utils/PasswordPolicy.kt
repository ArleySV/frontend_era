package com.era.app.utils

data class CriteriosContrasena(
    val longitudMinima: Boolean,
    val tieneMayuscula: Boolean,
    val tieneMinuscula: Boolean,
    val tieneNumero: Boolean,
    val tieneSimbolo: Boolean,
    val distintaDeUsuario: Boolean,
    val sinDatosPersonales: Boolean,
)

object PasswordPolicy {

    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 72

    fun criterios(
        contrasena: String,
        nombreUsuario: String,
        nombreMenor: String
    ): CriteriosContrasena {
        val tokensDelNombre = nombreMenor
            .trim()
            .split(Regex("\\s+"))
            .filter { it.length >= 3 }
        return CriteriosContrasena(
            longitudMinima = contrasena.length >= MIN_LENGTH,
            tieneMayuscula = contrasena.any { it.isUpperCase() },
            tieneMinuscula = contrasena.any { it.isLowerCase() },
            tieneNumero = contrasena.any { it.isDigit() },
            tieneSimbolo = contrasena.any { !it.isLetterOrDigit() && !it.isWhitespace() },
            distintaDeUsuario = !contrasena.equals(nombreUsuario, ignoreCase = true),
            sinDatosPersonales = tokensDelNombre.isEmpty() ||
                tokensDelNombre.none { contrasena.contains(it, ignoreCase = true) },
        )
    }

    fun errores(
        contrasena: String,
        nombreUsuario: String,
        nombreMenor: String
    ): List<String> {
        val c = criterios(contrasena, nombreUsuario, nombreMenor)
        val errores = mutableListOf<String>()

        if (!c.longitudMinima) {
            errores += "Debe tener al menos $MIN_LENGTH caracteres."
        }
        if (contrasena.length > MAX_LENGTH) {
            errores += "Máximo $MAX_LENGTH caracteres."
        }
        if (!c.tieneMayuscula) {
            errores += "Debe incluir al menos una mayúscula."
        }
        if (!c.tieneMinuscula) {
            errores += "Debe incluir al menos una minúscula."
        }
        if (!c.tieneNumero) {
            errores += "Debe incluir al menos un número."
        }
        if (!c.tieneSimbolo) {
            errores += "Debe incluir al menos un símbolo."
        }
        if (!c.distintaDeUsuario) {
            errores += "No puede ser igual al nombre de usuario."
        }
        if (!c.sinDatosPersonales) {
            errores += "No puede contener datos personales."
        }

        return errores
    }

    fun esValida(
        contrasena: String,
        nombreUsuario: String,
        nombreMenor: String
    ): Boolean = errores(contrasena, nombreUsuario, nombreMenor).isEmpty()
}
