package com.emm.gema.data.network.attendance

import kotlinx.serialization.Serializable

@Serializable
data class StudentRequest(
    val studentId: String,
    val status: String,
)
