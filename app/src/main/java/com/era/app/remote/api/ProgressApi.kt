package com.era.app.remote.api

import com.era.app.remote.dto.progress.ProgressSyncRequest
import com.era.app.remote.dto.progress.ProgressSyncResponse
import com.era.app.remote.dto.progress.ResetProgressRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProgressApi {

    @GET("progress/sync")
    suspend fun getProgress(): ProgressSyncResponse

    @POST("progress/sync")
    suspend fun syncProgress(@Body request: ProgressSyncRequest): ProgressSyncResponse

    @POST("progress/reset")
    suspend fun resetProgress(@Body request: ResetProgressRequest): ProgressSyncResponse
}
