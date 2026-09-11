package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryStudentRepository : StudentRepository {

    private val students: MutableStateFlow<List<Student>> = MutableStateFlow(emptyList())

    override fun observeBySection(sectionId: String): Flow<List<Student>> = students
        .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<String, Int>> = students
        .map { stored -> stored.filterNot { it.isWithdrawn }.groupingBy { it.sectionId }.eachCount() }

    override suspend fun listBySection(sectionId: String): List<Student> = students.value
        .filter { it.sectionId == sectionId }
        .orderedByName()

    override suspend fun findById(id: String): Student? = students.value.find { it.id == id }

    override suspend fun findByCode(sectionId: String, code: StudentCode): Student? = students.value
        .find { it.sectionId == sectionId && it.code == code }

    override suspend fun save(student: Student) {
        students.value = students.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: String) {
        students.value = students.value.filterNot { it.sectionId == sectionId }
    }
}
