package com.era.app.repository

import com.era.app.remote.dto.auth.LoginRequest
import com.era.app.remote.dto.auth.LoginResponse
import com.era.app.remote.dto.auth.PasswordResetConfirmRequest
import com.era.app.remote.dto.auth.PasswordResetRequest
import com.era.app.remote.dto.auth.PasswordResetVerifyRequest
import com.era.app.remote.dto.auth.PasswordResetVerifyResponse
import com.era.app.remote.dto.auth.RegisterRequest
import com.era.app.remote.dto.auth.ResendOtpRequest
import com.era.app.remote.dto.auth.VerifyEmailRequest
import com.era.app.remote.dto.common.MessageResponse

interface AuthRepository {
    suspend fun register(request: RegisterRequest): Resultado<Unit>
    suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit>
    suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit>
    suspend fun login(request: LoginRequest): Resultado<LoginResponse>
    suspend fun logout(): Resultado<MessageResponse>
    suspend fun requestPasswordReset(request: PasswordResetRequest): Resultado<Unit>
    suspend fun verifyPasswordReset(request: PasswordResetVerifyRequest): Resultado<PasswordResetVerifyResponse>
    suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Resultado<Unit>
}
