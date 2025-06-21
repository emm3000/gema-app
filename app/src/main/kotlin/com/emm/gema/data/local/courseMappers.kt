package com.emm.gema.data.local

import com.emm.gema.domain.course.model.Course

fun CourseEntity.toDomain() = Course(
    id = id,
    name = name,
    grade = grade,
    section = section,
    level = level,
    shift = shift,
    academicYear = academic_year,
)

fun List<CourseEntity>.toDomain(): List<Course> = map(CourseEntity::toDomain)