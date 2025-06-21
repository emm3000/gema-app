package com.emm.gema.data.network.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthNetworkRepository(
    private val authApi: AuthApi,
    private val dataStore: DataStore,
) {

    suspend fun register(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        val registerRequest = RegisterRequest(
            name = name,
            email = email,
            password = password,
        )
        val response: AuthResponse = authApi.register(registerRequest)
        dataStore.storeToken(response.accessToken)
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        val registerRequest = LoginRequest(
            email = email,
            password = password,
        )
        val response: AuthResponse = authApi.login(registerRequest)
        dataStore.storeToken(response.accessToken)
    }
}