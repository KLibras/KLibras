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
    val actionFound: Boolean,
    val predictedAction: String,
    val confidence: String,
    val expectedAction: String,
    val isMatch: Boolean
)




// Resposta do login, tem que pegar essas infos e salvar no TokenManager
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



interface AuthService {


    /**

     "email" : email,
     "username" : username,
     "password"  : password

     **/
    @POST("register")
    suspend fun register(@Body user: RequestBody): Response<User>

    @Multipart
    @POST("login")
    suspend fun login(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody
    ): Response<LoginResponse>


    /**
     Quando o usuário já tiver autenticado ele tem 2 tokens

     access_token: Esse token tem uma vida curta, pode mudar mas por padrão tem 15 minutos, depois dos 15 minutos ele vai expirar e tem que chamar esse request pra renovar
     refresh_token: Esse token já é mais longo, ta com 60 minutos, o access_token só é renovado se o refresh_token não tiver expirado.

     precisa do access_token pra fazer os requests especiais
     **/
    @POST("refresh")
    suspend fun refreshToken(@Body refreshToken: RequestBody): Response<LoginResponse>
}

interface UserService {
    // Retorna o placar de usuários ordenado por pontos de forma decrescente
    @GET("leaderboard")
    suspend fun getLeaderboard(): Response<List<User>>

    // É usado pra verifcar qual usuário ta autenticado
    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>


    // É usado para adicionar ao usuário quando ele completar o modulo (Seria bom adicionar algo pra quando o
    // usuário termina o modulo, ou lá na tela de escolher o moduloo deixar tipo verde ou sla)
    @POST("users/me/modules/{module_id}")
    suspend fun addCompletedModule(@Path("module_id") moduleId: Int): Response<User>

    // É usado pra adicionar sinais ao usuário pra aparecer na dex depois
    @POST("users/me/signs/{sign_id}")
    suspend fun addKnownSign(@Path("sign_id") signId: Int): Response<User>

    // Usado pra pegar os modulos completos do usuario
    @GET("users/me/modules")
    suspend fun getCompletedModules(): Response<List<Module>>
    // Usado pra pegar os sinais completos do usuario. tipo lá na tela do dex ou na tela de conta teria quer ser tipo List<Sign>.size();
    @GET("users/me/signs")
    suspend fun getKnownSigns(): Response<List<Sign>>

    // Update username, precisa ta autenticado
    @PATCH("users/me/username")
    suspend fun updateUsername(@Body usernameUpdate: UsernameUpdate): Response<User>

    // Update password, precisa ta autenticado
    @PATCH("users/me/password")
    suspend fun updatePassword(@Body passwordUpdate: PasswordUpdate): Response<Unit>

    // Pra pegar as infos do modulo, dentro desse modulo que retorna tem uma lista de sinais, então tem que refactorar o gesturelearning
    @GET("get_module/{name}")
    suspend fun getModuleByName(@Path("name") name: String): Response<Module>
}

interface RecognitionService {


    /**

    Isso aqui é meio complicado
    Por causa do aio-pika/rabbitMq que é pra conseguir rodar o reconhecimento do sinal em background pra não travar o server
    então você manda o check_action, ele vai retornar esse job que depois tem que ser pego quando completo e nele vai ter o resultado.


    **/
    @Multipart
    @POST("/check_action")
    fun uploadVideo(
        @Part("expected_action") expectedAction: String,
        @Part video: MultipartBody.Part
    ): Call<UploadResponse>

    @GET("/results/{job_id}")
    fun getResult(@Path("job_id") jobId: String): Call<ResultResponse>
}