package com.emm.gema.data.attendance

import kotlinx.serialization.Serializable

@Serializable
data class StudentRequest(
    val studentId: String,
    val status: String,
)
