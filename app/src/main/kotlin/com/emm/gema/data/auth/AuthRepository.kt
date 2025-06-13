package com.emm.gema.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val loginService: LoginService,
    private val dataStore: DataStore,
) {

    suspend fun register(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        val registerRequest = RegisterRequest(
            name = name,
            email = email,
            password = password,
        )
        val response: AuthResponse = loginService.register(registerRequest)
        dataStore.storeToken(response.accessToken)
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        val registerRequest = LoginRequest(
            email = email,
            password = password,
        )
        val response: AuthResponse = loginService.login(registerRequest)
        dataStore.storeToken(response.accessToken)
    }
}