package com.era.app.repository

interface SesionRepository {
    fun guardarToken(token: String)
    fun obtenerToken(): String?
    fun limpiarToken()
    fun tieneToken(): Boolean
}
