package com.br.klibras.core.service.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class JobResponse(
    val job_id: String,
    val status: String
)


data class AnalysisResult(
    val action_found: Boolean,
    val predicted_action: String,
    val confidence: String,
    val is_match: Boolean
)


data class ResultResponse(
    val status: String,
    val result: AnalysisResult?,
    val error: String?
)


interface UserAuthService {
    @Multipart
    @POST("login")
    suspend fun login(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): Response<LoginResponse>
}

interface RecognitionService {
    @Multipart
    @POST("/check_action")
    suspend fun uploadForAnalysis(
        @Part expected_action: MultipartBody.Part,
        @Part video: MultipartBody.Part
    ): Response<JobResponse>

    @GET("/results/{job_id}")
    suspend fun getAnalysisResult(
        @Path("job_id") jobId: String
    ): Response<ResultResponse>
}

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)