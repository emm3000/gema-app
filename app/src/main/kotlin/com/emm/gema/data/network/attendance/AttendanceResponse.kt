package com.emm.gema.data.network.attendance

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceResponse(
    val student: StudentResponse,
    val status: String,
)