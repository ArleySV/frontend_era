package com.era.app.repository

import com.era.app.utils.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManagerSesion @Inject constructor(
    private val tokenManager: TokenManager,
) : SesionRepository {

    override fun guardarToken(token: String) = tokenManager.saveToken(token)

    override fun obtenerToken(): String? = tokenManager.getToken()

    override fun limpiarToken() = tokenManager.clearToken()

    override fun tieneToken(): Boolean = tokenManager.hasToken()
}
