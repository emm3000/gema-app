package com.emm.gema.feat.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val isValidFields: Boolean = false,
    val successLogin: Boolean = false,
)