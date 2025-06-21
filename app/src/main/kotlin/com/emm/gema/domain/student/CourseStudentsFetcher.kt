package com.emm.gema.domain.student

import kotlinx.coroutines.flow.Flow

class CourseStudentsFetcher(private val studentRepository: StudentRepository) {

    fun fetch(courseId: String): Flow<List<Student>> = studentRepository.findByCourseId(courseId)
}