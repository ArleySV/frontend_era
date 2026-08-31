package com.era.app.repository

interface SesionRepository {
    fun guardarToken(token: String)
    fun obtenerToken(): String?
    fun guardarCorreo(correo: String)
    fun obtenerCorreo(): String?
    fun limpiarToken()
    fun tieneToken(): Boolean
}
