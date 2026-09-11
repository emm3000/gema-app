package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.fake.SequentialIdGenerator
import com.emm.gema.core.domain.section.SectionId
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private const val FIRST_CODE: String = "12345678901234"
private const val SECOND_CODE: String = "12345678901235"

class StudentUseCasesTest {

    private val repository = InMemoryStudentRepository()
    private val saveStudent = SaveStudentUseCase(repository, SequentialIdGenerator("student"))
    private val getStudents = GetStudentsUseCase(repository)
    private val getStudent = GetStudentUseCase(repository)
    private val getStudentCounts = GetStudentCountsUseCase(repository)
    private val withdrawStudent = WithdrawStudentUseCase(repository)
    private val reactivateStudent = ReactivateStudentUseCase(repository)

    @Test
    fun `a student is added to the section`() = runTest {
        val result: StudentSaveResult = add(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")

        assertThat(result).isInstanceOf(StudentSaveResult.Saved::class.java)
        val stored: Student = getStudents(sectionId).first().single()
        assertThat(stored.code.value).isEqualTo(FIRST_CODE)
        assertThat(stored.fullName).isEqualTo("ACOSTA RIVERA, Luz Maria")
        assertThat(stored.isWithdrawn).isFalse()
    }

    @Test
    fun `a name is stored without its surrounding blanks`() = runTest {
        add(FIRST_CODE, "  ACOSTA RIVERA, Luz Maria  ")

        assertThat(getStudents(sectionId).first().single().fullName).isEqualTo("ACOSTA RIVERA, Luz Maria")
    }

    @Test
    fun `a blank name is rejected`() = runTest {
        assertThat(add(FIRST_CODE, "   ")).isEqualTo(StudentSaveResult.BlankName)
        assertThat(getStudents(sectionId).first()).isEmpty()
    }

    @Test
    fun `a code that is not fourteen digits is rejected`() = runTest {
        assertThat(add("123", "ACOSTA RIVERA, Luz Maria")).isEqualTo(StudentSaveResult.InvalidCode)
        assertThat(getStudents(sectionId).first()).isEmpty()
    }

    @Test
    fun `a code already used in the section is rejected`() = runTest {
        add(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")

        assertThat(add(FIRST_CODE, "BAUTISTA QUISPE, Jose")).isEqualTo(StudentSaveResult.DuplicateCode)
        assertThat(getStudents(sectionId).first()).hasSize(1)
    }

    @Test
    fun `the same code is free in another section`() = runTest {
        add(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")

        val result: StudentSaveResult = saveStudent(
            sectionId = SectionId("section-2"),
            studentId = null,
            code = FIRST_CODE,
            fullName = "BAUTISTA QUISPE, Jose",
        )

        assertThat(result).isInstanceOf(StudentSaveResult.Saved::class.java)
    }

    @Test
    fun `editing a student keeps its identity and its code`() = runTest {
        val student: Student = savedStudent(FIRST_CODE, "ACOSTA RIVER, Luz Maria")

        saveStudent(sectionId, student.id, FIRST_CODE, "ACOSTA RIVERA, Luz Maria")

        val stored: Student = getStudent(student.id)!!
        assertThat(stored.fullName).isEqualTo("ACOSTA RIVERA, Luz Maria")
        assertThat(getStudents(sectionId).first()).hasSize(1)
    }

    @Test
    fun `editing a student into another student's code is rejected`() = runTest {
        val student: Student = savedStudent(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")
        savedStudent(SECOND_CODE, "BAUTISTA QUISPE, Jose")

        val result: StudentSaveResult = saveStudent(sectionId, student.id, SECOND_CODE, "ACOSTA RIVERA, Luz Maria")

        assertThat(result).isEqualTo(StudentSaveResult.DuplicateCode)
        assertThat(getStudent(student.id)?.code?.value).isEqualTo(FIRST_CODE)
    }

    @Test
    fun `withdrawing keeps the student and dates the withdrawal`() = runTest {
        val student: Student = savedStudent(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")

        withdrawStudent(student.id, LocalDate.of(2026, 9, 4))

        val stored: Student = getStudent(student.id)!!
        assertThat(stored.withdrawalDate).isEqualTo(LocalDate.of(2026, 9, 4))
        assertThat(stored.isWithdrawn).isTrue()
        assertThat(getStudents(sectionId).first()).hasSize(1)
    }

    @Test
    fun `reactivating clears the withdrawal date`() = runTest {
        val student: Student = savedStudent(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")
        withdrawStudent(student.id, LocalDate.of(2026, 9, 4))

        reactivateStudent(student.id)

        assertThat(getStudent(student.id)?.withdrawalDate).isNull()
    }

    @Test
    fun `the list is ordered by the siagie name, surnames first`() = runTest {
        add("12345678901236", "CCAHUANA MAMANI, Rosa")
        add(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")
        add(SECOND_CODE, "BAUTISTA QUISPE, Jose")

        assertThat(getStudents(sectionId).first().map { it.fullName })
            .containsExactly("ACOSTA RIVERA, Luz Maria", "BAUTISTA QUISPE, Jose", "CCAHUANA MAMANI, Rosa")
            .inOrder()
    }

    @Test
    fun `counts per section leave withdrawn students out`() = runTest {
        val student: Student = savedStudent(FIRST_CODE, "ACOSTA RIVERA, Luz Maria")
        add(SECOND_CODE, "BAUTISTA QUISPE, Jose")

        assertThat(getStudentCounts().first()[sectionId]).isEqualTo(2)

        withdrawStudent(student.id, LocalDate.of(2026, 9, 4))

        assertThat(getStudentCounts().first()[sectionId]).isEqualTo(1)
    }

    private suspend fun add(code: String, fullName: String): StudentSaveResult =
        saveStudent(sectionId = sectionId, studentId = null, code = code, fullName = fullName)

    private suspend fun savedStudent(code: String, fullName: String): Student {
        val result: StudentSaveResult = add(code, fullName)
        return (result as StudentSaveResult.Saved).student
    }
}
