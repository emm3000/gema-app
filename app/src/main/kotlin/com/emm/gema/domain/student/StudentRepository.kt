package com.emm.gema.domain.student

import kotlinx.coroutines.flow.Flow

interface StudentRepository {

    suspend fun create(student: Student): Student

    suspend fun update(studentId: String, student: Student): Student

    suspend fun find(studentId: String): Student?

    suspend fun delete(studentId: String)

    suspend fun all(): Flow<List<Student>>
}