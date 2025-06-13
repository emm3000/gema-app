package com.emm.gema.data.api

import com.emm.gema.data.api.model.RegisterRequest
import com.emm.gema.data.api.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginService {

    @POST("auth/login")
    suspend fun login(
        @Body request: RegisterRequest,
    ): RegisterResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): RegisterResponse
}