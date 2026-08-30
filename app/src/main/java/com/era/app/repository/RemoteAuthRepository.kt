package com.era.app.repository

import com.era.app.remote.api.AuthApi
import com.era.app.remote.dto.auth.LoginRequest
import com.era.app.remote.dto.auth.LoginResponse
import com.era.app.remote.dto.auth.PasswordResetConfirmRequest
import com.era.app.remote.dto.auth.PasswordResetRequest
import com.era.app.remote.dto.auth.PasswordResetVerifyRequest
import com.era.app.remote.dto.auth.PasswordResetVerifyResponse
import com.era.app.remote.dto.auth.RegisterRequest
import com.era.app.remote.dto.auth.ResendOtpRequest
import com.era.app.remote.dto.auth.VerifyEmailRequest
import com.era.app.remote.dto.common.ErrorResponse
import com.era.app.remote.dto.common.MessageResponse
import com.era.app.utils.ErrorMapper
import com.era.app.utils.EraError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAuthRepository @Inject constructor(
    private val api: AuthApi,
    private val json: Json,
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): Resultado<Unit> =
        llamar { api.register(request) }

    override suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit> =
        llamar { api.verifyEmail(request) }

    override suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit> =
        llamar { api.resendOtp(request) }

    override suspend fun login(request: LoginRequest): Resultado<LoginResponse> =
        llamar { api.login(request) }

    override suspend fun logout(): Resultado<MessageResponse> =
        llamar { api.logout() }

    override suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit> =
        llamar { api.requestPasswordReset(request) }

    override suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse> =
        llamar { api.verifyPasswordReset(request) }

    override suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit> =
        llamar { api.confirmPasswordReset(request) }

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
