package com.era.app.remote.api

import com.era.app.remote.dto.common.MessageResponse
import com.era.app.remote.dto.feedback.CommentRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedbackApi {

    @POST("feedback/comments")
    suspend fun sendComment(@Body request: CommentRequest): MessageResponse
}
