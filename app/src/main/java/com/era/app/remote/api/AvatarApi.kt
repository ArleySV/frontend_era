package com.era.app.remote.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface AvatarApi {

    @Multipart
    @PUT("users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Unit

    @GET("users/me/avatar")
    suspend fun getAvatar(): ResponseBody
}
