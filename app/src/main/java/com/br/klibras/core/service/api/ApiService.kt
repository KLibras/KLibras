package com.br.klibras.core.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


data class UploadResponse(
    val jobId: String,
    val status: String
)

data class ResultResponse(
    val jobId: String,
    val status: String,
    val actionFound: Boolean?,
    val predictedAction: String?,
    val confidence: String?,
    val expectedAction: String?,
    val isMatch: Boolean?,
    val error: String?,
    val createdAt: String?,
    val completedAt: String?,
    val message: String?
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val points: Int
)

data class Module(
    val id: Int,
    val name: String,
    val signs: List<Sign>
)

data class Sign(
    val id: Int,
    val name: String,
    val desc: String,
    val videoUrl: String,
    val pontos: Int
)

data class UsernameUpdate(
    val new_username: String
)

data class PasswordUpdate(
    val new_password: String
)

data class GoogleTokenRequest(
    val id_token: String
)

interface AuthService {

    @POST("register")
    suspend fun register(@Body user: RequestBody): Response<User>

    @Multipart
    @POST("login")
    suspend fun login(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): Response<LoginResponse>

    @POST("refresh")
    suspend fun refreshToken(@Body refreshToken: RequestBody): Response<LoginResponse>

    @POST("auth/google")
    suspend fun googleAuth(@Body token: GoogleTokenRequest): Response<LoginResponse>
}

interface UserService {
    @GET("leaderboard")
    suspend fun getLeaderboard(): Response<List<User>>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>

    @POST("users/me/modules/{module_id}")
    suspend fun addCompletedModule(@Path("module_id") moduleId: Int): Response<User>

    @POST("users/me/signs/{sign_id}")
    suspend fun addKnownSign(@Path("sign_id") signId: Int): Response<User>

    @GET("users/me/modules")
    suspend fun getCompletedModules(): Response<List<Module>>

    @GET("users/me/signs")
    suspend fun getKnownSigns(): Response<List<Sign>>

    @PATCH("users/me/username")
    suspend fun updateUsername(@Body usernameUpdate: UsernameUpdate): Response<User>

    @PATCH("users/me/password")
    suspend fun updatePassword(@Body passwordUpdate: PasswordUpdate): Response<Unit>

    @GET("get_module/{name}")
    suspend fun getModuleByName(@Path("name") name: String): Response<Module>
}


interface RecognitionService {
    @Multipart
    @POST("check_action")
    suspend fun uploadVideo(
        @Part("expected_action") action: RequestBody,
        @Part video: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("results/{jobId}")
    suspend fun getResult(
        @Path("jobId") jobId: String,
        @Query("wait") wait: Boolean = false,
        @Query("timeout") timeout: Int = 10
    ): Response<ResultResponse>
}