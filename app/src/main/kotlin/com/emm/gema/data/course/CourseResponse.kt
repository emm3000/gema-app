package com.emm.gema.data.course

import kotlinx.serialization.Serializable

@Serializable
data class CourseResponse(
    val id: String,
    val name: String,
    val grade: String,
    val section: String,
    val level: String,
    val shift: String,
    val academicYear: Int,
) {

    override fun toString(): String {
        return name
    }
}