package com.era.app.repository

import com.era.app.remote.api.FeedbackApi
import com.era.app.remote.dto.common.ErrorResponse
import com.era.app.remote.dto.feedback.CommentRequest
import com.era.app.utils.EraError
import com.era.app.utils.ErrorMapper
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteFeedbackRepository @Inject constructor(
    private val api: FeedbackApi,
    private val sesionRepository: SesionRepository,
    private val json: Json
) : FeedbackRepository {

    override suspend fun enviarComentario(contenido: String): Resultado<Unit> {
        // Validación local previa (redundante con el VM por seguridad)
        if (contenido.isBlank() || contenido.length > 2000) {
            return Resultado.Fallo(EraError.Validacion(listOf("Contenido inválido")))
        }

        // NO LOGUEAR 'contenido' (Regla de Oro)
        val resultado = llamar { api.sendComment(CommentRequest(contenido)) }

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
