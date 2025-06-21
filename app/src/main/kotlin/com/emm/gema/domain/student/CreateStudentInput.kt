package com.emm.gema.domain.student

data class CreateStudentInput(
    val fullName: String,
    val dni: String,
    val email: String,
    val birthDate: String,
    val gender: String,
    val courseId: String,
)