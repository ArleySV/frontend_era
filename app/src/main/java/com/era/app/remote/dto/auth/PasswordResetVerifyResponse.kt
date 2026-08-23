package com.era.app.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetVerifyResponse(
    @SerialName("reset_token")
    val resetToken: String,
)
