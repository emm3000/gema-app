package com.emm.gema.data.attendance

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceResponse(
    val student: StudentResponse,
    val status: String,
)