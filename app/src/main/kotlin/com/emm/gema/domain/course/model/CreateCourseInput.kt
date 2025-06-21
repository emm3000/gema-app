package com.emm.gema.domain.course.model

data class CreateCourseInput(
    val name: String,
    val grade: String,
    val section: String,
    val level: String,
    val shift: String,
    val academicYear: Long,
)
