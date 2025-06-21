package com.emm.gema.domain.student

import kotlinx.coroutines.flow.Flow

interface StudentRepository {

    suspend fun create(student: CreateStudentInput): StudentId

    suspend fun update(studentId: String, student: Student): Student

    suspend fun find(studentId: String): Student?

    suspend fun delete(studentId: String)

    fun all(): Flow<List<Student>>

    fun findByCourseId(courseId: String): Flow<List<Student>>
}