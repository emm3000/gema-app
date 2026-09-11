package com.emm.gema.feature.students

import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeStudentRepository(initial: List<Student> = emptyList()) : StudentRepository {

    val students: MutableStateFlow<List<Student>> = MutableStateFlow(initial)
    var failsOnce: Boolean = false

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
        failOnce()
        students.value = students.value.filterNot { it.id == student.id } + student
    }

    override suspend fun deleteBySection(sectionId: String) {
        students.value = students.value.filterNot { it.sectionId == sectionId }
    }

    private fun failOnce() {
        if (!failsOnce) return
        failsOnce = false
        error("the disk is full")
    }
}

class FakeSectionRepository(private val sections: List<Section>) : SectionRepository {

    override fun observeBySchoolYear(schoolYearId: String): Flow<List<Section>> =
        MutableStateFlow(sections.filter { it.schoolYearId == schoolYearId })

    override fun observeCountsBySchoolYear(): Flow<Map<String, Int>> =
        MutableStateFlow(sections.groupingBy { it.schoolYearId }.eachCount())

    override suspend fun findById(id: String): Section? = sections.find { it.id == id }

    override suspend fun save(section: Section) = Unit

    override suspend fun delete(id: String) = Unit
}
