package com.era.app.remote.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class DeleteAccountRequest(
    val contrasena: String,
)
