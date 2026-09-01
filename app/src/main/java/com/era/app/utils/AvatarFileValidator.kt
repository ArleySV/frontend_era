package com.era.app.utils

import com.era.app.repository.Resultado

data class ArchivoAvatar(
    val bytes: ByteArray,
    val filename: String?,
    val mimeType: String,
)

object AvatarFileValidator {

    const val MAX_BYTES_AVATAR: Long = 2L * 1024L * 1024L

    private val mimesPermitidos = setOf("image/jpeg", "image/png", "image/webp")

    /**
     * Valida los datos ya leídos de la imagen (bytes + metadatos) antes de subirla.
     * Helper puro: no depende de Android, testeable en JVM.
     * Cero logs de bytes/filename (D-61).
     */
    fun validar(
        bytes: ByteArray,
        filename: String?,
        mimeType: String?,
    ): Resultado<ArchivoAvatar> {
        if (mimeType == null || mimeType.isBlank() || mimeType !in mimesPermitidos) {
            return Resultado.Fallo(EraError.Validacion(listOf("Formato no soportado")))
        }
        if (bytes.isEmpty()) {
            return Resultado.Fallo(EraError.Validacion(listOf("El archivo está vacío")))
        }
        if (bytes.size.toLong() > MAX_BYTES_AVATAR) {
            return Resultado.Fallo(EraError.Validacion(listOf("La imagen no puede superar 2 MB")))
        }
        return Resultado.Exito(ArchivoAvatar(bytes, filename, mimeType))
    }

    /**
     * Sobrecarga para validar SIN haber leído el binario en memoria (D-57): cuando el
     * composable ya conoce el tamaño del archivo vía `AssetFileDescriptor.length` y
     * excede el límite, evita cargar el `ByteArray` (no fabrica un array sintético).
     * El "éxito" teórico con bytes vacíos no se da: un tamaño > MAX siempre falla.
     */
    fun validar(size: Long, mimeType: String?): Resultado<ArchivoAvatar> {
        if (mimeType == null || mimeType.isBlank() || mimeType !in mimesPermitidos) {
            return Resultado.Fallo(EraError.Validacion(listOf("Formato no soportado")))
        }
        if (size > MAX_BYTES_AVATAR) {
            return Resultado.Fallo(EraError.Validacion(listOf("La imagen no puede superar 2 MB")))
        }
        return Resultado.Exito(ArchivoAvatar(ByteArray(0), null, mimeType))
    }
}
