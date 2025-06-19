package com.emm.gema.data.attendance

import kotlinx.serialization.Serializable

@Serializable
data class StudentResponse(
    val id: String,
    val fullName: String,
    val dni: String?,
    val email: String?,
    val birthDate: String?,
    val gender: String?,
)