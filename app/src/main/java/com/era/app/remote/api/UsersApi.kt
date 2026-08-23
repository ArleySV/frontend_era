package com.era.app.remote.api

import com.era.app.remote.dto.common.MessageResponse
import com.era.app.remote.dto.user.DeleteAccountRequest
import com.era.app.remote.dto.user.UpdateUsernameRequest
import com.era.app.remote.dto.user.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH

interface UsersApi {

    @GET("users/me")
    suspend fun getProfile(): UserProfile

    @PATCH("users/me")
    suspend fun updateUsername(@Body request: UpdateUsernameRequest): UserProfile

    @HTTP(method = "DELETE", path = "users/me", hasBody = true)
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): MessageResponse
}
