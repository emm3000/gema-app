package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.fake.InMemorySchoolYearRepository
import com.emm.gema.core.domain.fake.InMemorySectionRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CountAllStudentsUseCaseTest {

    private val schoolYearRepository = InMemorySchoolYearRepository()
    private val sectionRepository = InMemorySectionRepository()
    private val studentRepository = InMemoryStudentRepository()
    private val countAllStudents = CountAllStudentsUseCase(
        schoolYearRepository,
        sectionRepository,
        studentRepository,
    )

    @Test
    fun `counts active and withdrawn students across sections and school years`() = runTest {
        val firstYear = SchoolYear(
            id = SchoolYearId("year-1"),
            label = "2025",
            startDate = LocalDate.of(2025, 3, 1),
            endDate = LocalDate.of(2025, 12, 15),
            periodKind = PeriodKind.BIMESTER,
        )
        val secondYear = SchoolYear(
            id = SchoolYearId("year-2"),
            label = "2026",
            startDate = LocalDate.of(2026, 3, 1),
            endDate = LocalDate.of(2026, 12, 15),
            periodKind = PeriodKind.BIMESTER,
        )
        schoolYearRepository.save(firstYear)
        schoolYearRepository.save(secondYear)

        val firstSection = Section(SectionId("section-1"), firstYear.id, Grade.FIRST, "A")
        val secondSection = Section(SectionId("section-2"), secondYear.id, Grade.SECOND, "B")
        sectionRepository.save(firstSection)
        sectionRepository.save(secondSection)

        studentRepository.save(aStudent("student-1", firstSection.id, "12345678901234"))
        studentRepository.save(
            aStudent("student-2", firstSection.id, "12345678901235", withdrawalDate = LocalDate.of(2025, 9, 4)),
        )
        studentRepository.save(aStudent("student-3", secondSection.id, "12345678901236"))

        assertThat(countAllStudents()).isEqualTo(3)
    }

    @Test
    fun `an empty device counts zero`() = runTest {
        assertThat(countAllStudents()).isEqualTo(0)
    }

    private fun aStudent(
        id: String,
        sectionId: SectionId,
        code: String,
        withdrawalDate: LocalDate? = null,
    ): Student = Student(
        id = StudentId(id),
        sectionId = sectionId,
        code = StudentCode(code),
        fullName = "ACOSTA RIVERA, Luz Maria",
        withdrawalDate = withdrawalDate,
    )
}
