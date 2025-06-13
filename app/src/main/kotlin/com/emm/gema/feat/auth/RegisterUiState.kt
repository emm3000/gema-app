package com.emm.gema.feat.auth

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isValidFields: Boolean = false,
    val isChecked: Boolean = false,
    val success: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)