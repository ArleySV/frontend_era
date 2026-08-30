package com.era.app.repository

import com.era.app.remote.api.UsersApi
import com.era.app.remote.dto.common.ErrorResponse
import com.era.app.remote.dto.user.DeleteAccountRequest
import com.era.app.remote.dto.user.UpdateUsernameRequest
import com.era.app.remote.dto.user.UserProfile
import com.era.app.utils.ErrorMapper
import com.era.app.utils.EraError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteUserRepository @Inject constructor(
    private val api: UsersApi,
    private val json: Json,
) : UserRepository {

    override suspend fun obtenerPerfil(): Resultado<UserProfile> =
        llamar { api.getProfile() }

    override suspend fun actualizarNombreUsuario(nombre: String): Resultado<UserProfile> =
        llamar { api.updateUsername(UpdateUsernameRequest(nombre)) }

    override suspend fun eliminarCuenta(contrasena: String): Resultado<Unit> =
        llamar { api.deleteAccount(DeleteAccountRequest(contrasena)) }

    private suspend fun <T> llamar(bloque: suspend () -> T): Resultado<T> =
        try {
            Resultado.Exito(bloque())
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            Resultado.Fallo(aEraError(t))
        }

    private fun aEraError(t: Throwable): EraError = when (t) {
        is HttpException -> {
            val body = t.response()?.errorBody()?.string()
                ?.let { raw -> runCatching { json.decodeFromString<ErrorResponse>(raw) }.getOrNull() }
            ErrorMapper.desdeHttp(t.code(), body)
        }
        else -> ErrorMapper.desdeThrowable(t)
    }
}
