package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.section.SectionId
import kotlinx.coroutines.flow.Flow

interface StudentRepository {

    fun observeBySection(sectionId: SectionId): Flow<List<Student>>

    fun observeCountsBySection(): Flow<Map<SectionId, Int>>

    suspend fun listBySection(sectionId: SectionId): List<Student>

    suspend fun findById(id: StudentId): Student?

    suspend fun findByCode(sectionId: SectionId, code: StudentCode): Student?

    suspend fun save(student: Student)

    suspend fun deleteBySection(sectionId: SectionId)
}
