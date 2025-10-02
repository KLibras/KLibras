package com.br.klibras.core.utils

import android.content.Context
import com.br.klibras.core.service.api.RecognitionService
import com.br.klibras.core.service.api.UserAuthService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://18.231.60.231:8000/" // Mudar depois pra URL certa

    private fun getAuthenticatedClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getUserAuthApi(context: Context): UserAuthService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserAuthService::class.java)
    }


    fun getRecognitionApi(context: Context): RecognitionService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context)) // This line ensures the JWT is sent
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecognitionService::class.java)
    }
}