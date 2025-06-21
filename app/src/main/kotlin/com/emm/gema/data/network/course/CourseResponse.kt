package com.emm.gema.data.network.course

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray

@Serializable
data class CourseResponse(
    val id: String,
    val name: String,
    val grade: String,
    val section: String,
    val level: String,
    val shift: String,
    val academicYear: Int,
    val student: JsonArray,
) {

    override fun toString(): String {
        return name
    }
}