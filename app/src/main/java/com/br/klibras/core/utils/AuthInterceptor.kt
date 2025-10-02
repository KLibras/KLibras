package com.br.klibras.core.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

// The Interceptor CORRECTLY takes a Context
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Use the Context to get the token from your TokenManager object
        val token = TokenManager.getAccessToken(context)

        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            // Add the header if the token exists
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}