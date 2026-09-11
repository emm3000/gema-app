package com.emm.gema.core.database.student

import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.UuidIdGenerator
import com.emm.gema.core.database.inMemoryGemaDb
import com.emm.gema.core.database.section.SqlDelightSectionAreaRepository
import com.emm.gema.core.database.curriculum.SqlDelightWorkedCompetencyRepository
import com.emm.gema.core.database.section.SqlDelightSectionRepository
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.section.CreateSectionUseCase
import com.emm.gema.core.domain.section.DeleteSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.GetStudentCountsUseCase
import com.emm.gema.core.domain.student.GetStudentUseCase
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.ReactivateStudentUseCase
import com.emm.gema.core.domain.student.SaveStudentUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.StudentSaveResult
import com.emm.gema.core.domain.student.WithdrawStudentUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

private const val FIRST_CODE: String = "12345678901234"
private const val SECOND_CODE: String = "12345678901235"

@OptIn(ExperimentalCoroutinesApi::class)
class StudentPersistenceTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val database: GemaDb = inMemoryGemaDb()
    private val sectionRepository: SectionRepository = SqlDelightSectionRepository(database, dispatcher)
    private val sectionAreaRepository: SectionAreaRepository = SqlDelightSectionAreaRepository(database, dispatcher)
    private val studentRepository: StudentRepository = SqlDelightStudentRepository(database, dispatcher)
    private val workedCompetencyRepository: WorkedCompetencyRepository =
        SqlDelightWorkedCompetencyRepository(database, dispatcher)

    private val createSection = CreateSectionUseCase(sectionRepository, UuidIdGenerator())
    private val deleteSection = DeleteSectionUseCase(
        sectionRepository,
        sectionAreaRepository,
        workedCompetencyRepository,
        studentRepository,
    )
    private val saveStudent = SaveStudentUseCase(studentRepository, UuidIdGenerator())
    private val getStudents = GetStudentsUseCase(studentRepository)
    private val getStudent = GetStudentUseCase(studentRepository)
    private val getStudentCounts = GetStudentCountsUseCase(studentRepository)
    private val withdrawStudent = WithdrawStudentUseCase(studentRepository)
    private val reactivateStudent = ReactivateStudentUseCase(studentRepository)

    @Test
    fun `a student is persisted under its section`() = runTest {
        val section: Section = section()

        saveStudent(section.id, null, FIRST_CODE, "ACOSTA RIVERA, Luz Maria")

        val stored: Student = getStudents(section.id).first().single()
        assertThat(stored.code.value).isEqualTo(FIRST_CODE)
        assertThat(stored.fullName).isEqualTo("ACOSTA RIVERA, Luz Maria")
        assertThat(stored.siagieId).isNull()
        assertThat(stored.withdrawalDate).isNull()
    }

    @Test
    fun `students are read back ordered by the siagie name`() = runTest {
        val section: Section = section()
        saveStudent(section.id, null, "12345678901236", "CCAHUANA MAMANI, Rosa")
        saveStudent(section.id, null, FIRST_CODE, "ACOSTA RIVERA, Luz Maria")
        saveStudent(section.id, null, SECOND_CODE, "BAUTISTA QUISPE, Jose")

        assertThat(getStudents(section.id).first().map { it.fullName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose", "CCAHUANA MAMANI, Rosa")
            .inOrder()
    }

    @Test
    fun `a code is unique inside a section and free in another one`() = runTest {
        val section: Section = section()
        val other: Section = createSection(section.schoolYearId, Grade.FOURTH, "B")
        saveStudent(section.id, null, FIRST_CODE, "ACOSTA RIVERA, Luz Maria")

        assertThat(saveStudent(section.id, null, FIRST_CODE, "BAUTISTA QUISPE, Jose"))
            .isEqualTo(StudentSaveResult.DuplicateCode)
        assertThat(saveStudent(other.id, null, FIRST_CODE, "BAUTISTA QUISPE, Jose"))
            .isInstanceOf(StudentSaveResult.Saved::class.java)
    }

    @Test
    fun `a withdrawal date survives a read back and can be cleared`() = runTest {
        val section: Section = section()
        val student: Student = savedStudent(section.id)

        withdrawStudent(student.id, LocalDate.of(2026, 9, 4))
        assertThat(getStudent(student.id)?.withdrawalDate).isEqualTo(LocalDate.of(2026, 9, 4))
        assertThat(getStudentCounts().first()[section.id]).isNull()

        reactivateStudent(student.id)
        assertThat(getStudent(student.id)?.withdrawalDate).isNull()
        assertThat(getStudentCounts().first()[section.id]).isEqualTo(1)
    }

    @Test
    fun `a deleted section takes its students with it`() = runTest {
        val section: Section = section()
        savedStudent(section.id)

        deleteSection(section.id)

        assertThat(getStudents(section.id).first()).isEmpty()
    }

    private suspend fun section(): Section = createSection("2026", Grade.THIRD, "A")

    private suspend fun savedStudent(sectionId: String): Student {
        val result: StudentSaveResult = saveStudent(sectionId, null, FIRST_CODE, "ACOSTA RIVERA, Luz Maria")
        return (result as StudentSaveResult.Saved).student
    }
}
