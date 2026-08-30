package com.era.app.repository

import com.era.app.remote.dto.user.UserProfile

interface UserRepository {
    suspend fun obtenerPerfil(): Resultado<UserProfile>
    suspend fun actualizarNombreUsuario(nombre: String): Resultado<UserProfile>
    suspend fun eliminarCuenta(contrasena: String): Resultado<Unit>
}
