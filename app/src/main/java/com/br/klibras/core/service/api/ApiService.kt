package com.br.klibras.core.service.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

// --- Data Classes ---

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class AnalysisResponse(
    val action_found: Boolean,
    val predicted_action: String,
    val confidence: String,
    val is_match: Boolean
)

// --- API Interfaces ---

interface UserAuthService {
    @Multipart // Use for multipart form data
    @POST("login")
    suspend fun login(
        // Use @Part and RequestBody with @Multipart
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): Response<LoginResponse>
}

interface RecognitionService {
    @Multipart
    @POST("action/check_action/")
    suspend fun uploadForAnalysis(
        @Part expected_action: MultipartBody.Part,
        @Part video: MultipartBody.Part
    ): Response<AnalysisResponse>
}