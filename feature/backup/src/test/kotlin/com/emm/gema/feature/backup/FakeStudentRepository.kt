package com.emm.gema.feature.backup

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeStudentRepository(
    vararg students: Student,
) : StudentRepository {

    private val stored: MutableStateFlow<List<Student>> = MutableStateFlow(students.toList())

    override fun observeBySection(sectionId: SectionId): Flow<List<Student>> =
        throw UnsupportedOperationException("not needed by BackupViewModelTest")

    override fun observeCountsBySection(): Flow<Map<SectionId, Int>> =
        throw UnsupportedOperationException("not needed by BackupViewModelTest")

    override suspend fun listBySection(sectionId: SectionId): List<Student> =
        stored.value.filter { it.sectionId == sectionId }

    override suspend fun findById(id: StudentId): Student? =
        throw UnsupportedOperationException("not needed by BackupViewModelTest")

    override suspend fun findByCode(sectionId: SectionId, code: StudentCode): Student? =
        throw UnsupportedOperationException("not needed by BackupViewModelTest")

    override suspend fun save(student: Student) {
        throw UnsupportedOperationException("not needed by BackupViewModelTest")
    }

    override suspend fun deleteBySection(sectionId: SectionId) {
        throw UnsupportedOperationException("not needed by BackupViewModelTest")
    }
}
