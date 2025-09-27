package com.br.klibras.core.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String, // Fala username mas é email, é que o OAUth da api só aceita dois paramentros (username,password)
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("token_type")
    val tokenType: String
)

data class User(
    val id: Int,
    val email: String,
    val username: String
)

interface UserService {


    @POST("register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<User>

    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

}