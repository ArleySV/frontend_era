package com.era.app.repository

import com.era.app.remote.dto.auth.LoginRequest
import com.era.app.remote.dto.auth.LoginResponse
import com.era.app.remote.dto.auth.RegisterRequest
import com.era.app.remote.dto.auth.ResendOtpRequest
import com.era.app.remote.dto.auth.VerifyEmailRequest

interface AuthRepository {
    suspend fun register(request: RegisterRequest): Resultado<Unit>
    suspend fun verifyEmail(request: VerifyEmailRequest): Resultado<Unit>
    suspend fun resendOtp(request: ResendOtpRequest): Resultado<Unit>
    suspend fun login(request: LoginRequest): Resultado<LoginResponse>
}
