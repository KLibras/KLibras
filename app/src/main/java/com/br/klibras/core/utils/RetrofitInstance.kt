package com.br.klibras.core.utils

import android.content.Context
import com.br.klibras.core.service.api.RecognitionService
import com.br.klibras.core.service.api.UserAuthService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // The base URL is now correct.
    private const val BASE_URL = "http://18.231.60.231:8000/"

    /**
     * Creates an OkHttpClient with an authentication interceptor and increased timeouts.
     * The timeouts are crucial for preventing errors when uploading larger files like videos.
     */
    private fun getAuthenticatedClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            // Set a 30-second timeout for connecting, reading, and writing.
            // This is essential for handling video uploads without timing out.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Creates and returns an instance of the UserAuthService API.
     */
    fun getUserAuthApi(context: Context): UserAuthService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserAuthService::class.java)
    }

    /**
     * Creates and returns an instance of the RecognitionService API.
     * This uses the authenticated client to ensure the JWT is sent with each request.
     */
    fun getRecognitionApi(context: Context): RecognitionService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context)) // This line ensures the JWT is sent
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecognitionService::class.java)
    }
}