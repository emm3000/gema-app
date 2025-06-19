package com.emm.gema.data.student

import kotlinx.serialization.Serializable

@Serializable
data class CreateStudentRequest(

    val fullName: String,

    val email: String,

    val dni: String,

    val gender: String,

    val coursesId: List<String>
)
