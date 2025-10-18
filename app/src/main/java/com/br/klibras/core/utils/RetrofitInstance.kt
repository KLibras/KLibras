package com.br.klibras.core.utils

import android.content.Context
import com.br.klibras.core.service.api.AuthService
import com.br.klibras.core.service.api.RecognitionService
import com.br.klibras.core.service.api.UserService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.15.8:8000/" // Mudar depois pra URL certa

    private fun getAuthenticatedClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getUserAuthApi(context: Context): AuthService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    fun getUserServiceApi(context: Context): UserService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserService::class.java)
    }

    fun getVideoProcessingService(context: Context): RecognitionService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecognitionService::class.java)
    }
}

