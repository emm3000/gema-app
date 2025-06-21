package com.emm.gema.data.network.auth

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class AuthInterceptor(private val dataStore: DataStore): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request.Builder = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer ${dataStore.token}")
        return chain.proceed(request.build())
    }
}