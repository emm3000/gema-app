package com.emm.gema.domain.course.model

data class Course(
    val id: String,
    val name: String,
    val grade: String,
    val section: String,
    val level: String,
    val shift: String,
    val academicYear: Long,
) {

    override fun toString(): String {
        return name
    }
}