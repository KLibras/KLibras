package com.br.klibras.core.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()


        val token = TokenManager.getAccessToken(context)

        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {

            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}