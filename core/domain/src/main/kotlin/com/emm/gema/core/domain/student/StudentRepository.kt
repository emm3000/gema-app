package com.emm.gema.core.domain.student

import kotlinx.coroutines.flow.Flow

interface StudentRepository {

    fun observeBySection(sectionId: String): Flow<List<Student>>

    fun observeCountsBySection(): Flow<Map<String, Int>>

    suspend fun findById(id: String): Student?

    suspend fun findByCode(sectionId: String, code: StudentCode): Student?

    suspend fun save(student: Student)

    suspend fun deleteBySection(sectionId: String)
}
