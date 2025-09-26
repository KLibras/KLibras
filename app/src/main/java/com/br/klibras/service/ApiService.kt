package com.br.klibras.service

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class AnalysisResponse(
    val action_found: Boolean,
    val predicted_action: String,
    val confidence: String,
    val is_match: Boolean
)

interface ApiService {
    @Multipart
    @POST("/check_action/")
    suspend fun checkAction(
        @Part expectedAction: MultipartBody.Part,
        @Part video: MultipartBody.Part
    ): Response<AnalysisResponse>
}