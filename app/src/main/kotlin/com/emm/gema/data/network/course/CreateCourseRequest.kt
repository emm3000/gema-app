package com.emm.gema.data.network.course

import kotlinx.serialization.Serializable

@Serializable
data class CreateCourseRequest(

    val academicYear: Int,

    val grade: String,

    val level: String,

    val name: String,

    val section: String,

    val shift: String,
)