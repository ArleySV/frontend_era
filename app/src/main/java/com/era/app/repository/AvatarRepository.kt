package com.era.app.repository

interface AvatarRepository {
    suspend fun subirAvatar(bytes: ByteArray, filename: String?, mimeType: String): Resultado<Unit>
    suspend fun obtenerAvatarBytes(): Resultado<ByteArray>
}
