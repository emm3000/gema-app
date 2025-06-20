package com.emm.gema.data.attendance

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRequest(
    val date: String,
    val courseId: String,
    val students: List<StudentRequest>
)
