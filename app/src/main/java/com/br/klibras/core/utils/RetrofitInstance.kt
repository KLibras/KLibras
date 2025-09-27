package com.br.klibras.util

import com.br.klibras.core.service.api.ApiService
import com.br.klibras.core.service.api.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {


    private const val BASE_URL = "http://10.0.2.2:8000" // URL da api, precisa mudar depois

    // Lazily create a Retrofit instance using Gson
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userApiService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

