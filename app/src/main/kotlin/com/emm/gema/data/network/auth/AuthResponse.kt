package com.emm.gema.data.network.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(

    @SerialName("access_token")
    val accessToken: String,
)
