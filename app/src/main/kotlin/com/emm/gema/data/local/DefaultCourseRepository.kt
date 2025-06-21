package com.emm.gema.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.CourseQueries
import com.emm.gema.domain.course.CourseRepository
import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.course.model.CreateCourseInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

typealias CourseEntity = com.emm.gema.Course

class DefaultCourseRepository(private val dao: CourseQueries) : CourseRepository {

    override suspend fun create(course: CreateCourseInput) {
        dao.create(
            id = UUID.randomUUID().toString(),
            name = course.name,
            grade = course.grade,
            section = course.section,
            level = course.level,
            shift = course.shift,
            academic_year = course.academicYear,
        )
    }

    override suspend fun find(id: String): Course? = dao
        .findUnique(id)
        .executeAsOneOrNull()
        ?.toDomain()

    override fun all(): Flow<List<Course>> = dao
        .findMany()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map(List<CourseEntity>::toDomain)
}