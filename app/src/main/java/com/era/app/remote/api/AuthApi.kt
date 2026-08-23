package com.era.app.remote.api

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
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): MessageResponse

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): MessageResponse

    @POST("auth/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): MessageResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout(): MessageResponse

    @POST("auth/password-reset/request")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): MessageResponse

    @POST("auth/password-reset/verify")
    suspend fun verifyPasswordReset(@Body request: PasswordResetVerifyRequest): PasswordResetVerifyResponse

    @POST("auth/password-reset/confirm")
    suspend fun confirmPasswordReset(@Body request: PasswordResetConfirmRequest): MessageResponse
}
