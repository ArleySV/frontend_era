package com.era.app.repository

import com.era.app.remote.api.AvatarApi
import com.era.app.remote.dto.common.ErrorResponse
import com.era.app.utils.EraError
import com.era.app.utils.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAvatarRepository @Inject constructor(
    private val api: AvatarApi,
    private val sesionRepository: SesionRepository,
    private val json: Json,
) : AvatarRepository {

    override suspend fun subirAvatar(
        bytes: ByteArray,
        filename: String?,
        mimeType: String,
    ): Resultado<Unit> {
        // Sin logs del binario ni del filename (D-61).
        val nombre = filename?.takeIf { it.isNotBlank() } ?: "avatar${extensionDe(mimeType)}"
        val mediaType = mimeType.toMediaType()
        val part = MultipartBody.Part.createFormData(
            "avatar",
            nombre,
            bytes.toRequestBody(mediaType),
        )

        val resultado = llamar { api.uploadAvatar(part) }

        return when (resultado) {
            is Resultado.Exito -> Resultado.Exito(Unit)
            is Resultado.Fallo -> {
                if (resultado.error is EraError.SesionExpirada || resultado.error is EraError.CuentaInactiva) {
                    sesionRepository.limpiarToken()
                }
                Resultado.Fallo(resultado.error)
            }
        }
    }

    override suspend fun obtenerAvatarBytes(): Resultado<ByteArray> {
        val resultado = llamar { api.getAvatar().bytes() }

        return when (resultado) {
            is Resultado.Exito -> Resultado.Exito(resultado.data)
            is Resultado.Fallo -> {
                // 404 NOT_FOUND (PerfilNoEncontrado) NO limpia sesión: es el caso legítimo
                // de "el usuario usa preset, no hay foto personalizada" (D-54).
                if (resultado.error is EraError.SesionExpirada || resultado.error is EraError.CuentaInactiva) {
                    sesionRepository.limpiarToken()
                }
                Resultado.Fallo(resultado.error)
            }
        }
    }

    private fun extensionDe(mimeType: String): String = when (mimeType) {
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        else -> ""
    }

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
