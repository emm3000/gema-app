package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.SeedCurriculumUseCase
import com.emm.gema.core.domain.curriculum.SetCompetencyWorkedUseCase
import com.emm.gema.core.domain.fake.InMemoryCompetencyRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodLevelRepository
import com.emm.gema.core.domain.fake.InMemorySectionAreaRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.fake.InMemoryWorkedCompetencyRepository
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val periodId: PeriodId = PeriodId("period-1")

class GetPeriodLevelSummaryUseCaseTest {

    private val competencyRepository: InMemoryCompetencyRepository = InMemoryCompetencyRepository()
    private val workedCompetencyRepository: InMemoryWorkedCompetencyRepository = InMemoryWorkedCompetencyRepository()
    private val studentRepository: InMemoryStudentRepository = InMemoryStudentRepository()
    private val sectionAreaRepository: InMemorySectionAreaRepository = InMemorySectionAreaRepository()
    private val periodLevelRepository: InMemoryPeriodLevelRepository = InMemoryPeriodLevelRepository()

    private val setCompetencyWorked: SetCompetencyWorkedUseCase =
        SetCompetencyWorkedUseCase(workedCompetencyRepository)
    private val savePeriodLevel: SavePeriodLevelUseCase = SavePeriodLevelUseCase(periodLevelRepository)
    private val getSummary: GetPeriodLevelSummaryUseCase = GetPeriodLevelSummaryUseCase(
        sectionAreaRepository = sectionAreaRepository,
        competencyRepository = competencyRepository,
        workedCompetencyRepository = workedCompetencyRepository,
        studentRepository = studentRepository,
        periodLevelRepository = periodLevelRepository,
    )

    @Test
    fun `the summary holds one table per active area with a worked competency`() = runTest {
        seed()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.MATE, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(sectionId, periodId).first()

        assertThat(summary.areas.map { it.area }).containsExactly(Area.MATE, Area.PPSS).inOrder()
        assertThat(summary.areas.single { it.area == Area.PPSS }.grid.columns.map { it.id })
            .containsExactly(Competency.idOf(Area.PPSS, 1))
        assertThat(summary.areas.single { it.area == Area.MATE }.grid.columns.map { it.id })
            .containsExactly(Competency.idOf(Area.MATE, 1))
    }

    @Test
    fun `an area with no worked competency is not a table`() = runTest {
        seed()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(sectionId, periodId).first()

        assertThat(summary.areas.map { it.area }).containsExactly(Area.PPSS)
    }

    @Test
    fun `a competency never marked worked is excluded from its area's columns`() = runTest {
        seed()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(sectionId, periodId).first()

        assertThat(summary.areas.single().grid.columns.map { it.id })
            .containsExactly(Competency.idOf(Area.PPSS, 1))
    }

    @Test
    fun `a hidden area drops its table entirely`() = runTest {
        seed()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.MATE, 1), isWorked = true)
        sectionAreaRepository.setAreaHidden(sectionId, Area.MATE, isHidden = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(sectionId, periodId).first()

        assertThat(summary.areas.map { it.area }).containsExactly(Area.PPSS)
    }

    @Test
    fun `each area table has one row per active student ordered by name`() = runTest {
        seed()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "BAUTISTA QUISPE, Jose")
        addStudent("student-2", "ACOSTA RIVERA, Luz")
        addStudent("student-3", "DELGADO HUAMAN, Pedro", isWithdrawn = true)

        val summary: PeriodLevelSummary = getSummary(sectionId, periodId).first()

        assertThat(summary.areas.single().grid.rows.map { it.student.id })
            .containsExactly(secondStudentId, firstStudentId).inOrder()
    }

    @Test
    fun `a recorded level appears in its student and competency cell`() = runTest {
        seed()
        setCompetencyWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        val key: PeriodLevelKey = keyOf(firstStudentId, Competency.idOf(Area.PPSS, 1))
        savePeriodLevel(PeriodLevel(key).withAchievementLevel(AchievementLevel.AD))

        val summary: PeriodLevelSummary = getSummary(sectionId, periodId).first()

        assertThat(summary.areas.single().grid.rows.single().cells.single().achievementLevel)
            .isEqualTo(AchievementLevel.AD)
    }

    private suspend fun seed() {
        SeedCurriculumUseCase(competencyRepository).invoke()
    }

    private suspend fun addStudent(id: String, fullName: String, isWithdrawn: Boolean = false) {
        studentRepository.save(
            Student(
                id = StudentId(id),
                sectionId = sectionId,
                code = StudentCode("1234567890123${id.last()}"),
                fullName = fullName,
                withdrawalDate = LocalDate.of(2026, 5, 1).takeIf { isWithdrawn },
            ),
        )
    }

    private fun keyOf(studentId: StudentId, competencyId: CompetencyId): PeriodLevelKey = PeriodLevelKey(
        sectionId = sectionId,
        periodId = periodId,
        studentId = studentId,
        competencyId = competencyId,
    )
}

private val secondStudentId: StudentId = StudentId("student-2")

private val firstStudentId: StudentId = StudentId("student-1")
