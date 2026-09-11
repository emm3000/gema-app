package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.SeedCurriculumUseCase
import com.emm.gema.core.domain.curriculum.SetCompetencyWorkedUseCase
import com.emm.gema.core.domain.fake.InMemoryCompetencyRepository
import com.emm.gema.core.domain.fake.InMemoryPeriodLevelRepository
import com.emm.gema.core.domain.fake.InMemorySectionAreaRepository
import com.emm.gema.core.domain.fake.InMemoryStudentRepository
import com.emm.gema.core.domain.fake.InMemoryWorkedCompetencyRepository
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

private const val SECTION_ID: String = "section-1"
private const val PERIOD_ID: String = "period-1"

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
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), isWorked = true)
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.MATE, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(SECTION_ID, PERIOD_ID).first()

        assertThat(summary.areas.map { it.area }).containsExactly(Area.MATE, Area.PPSS).inOrder()
        assertThat(summary.areas.single { it.area == Area.PPSS }.grid.columns.map { it.id })
            .containsExactly(Competency.idOf(Area.PPSS, 1))
        assertThat(summary.areas.single { it.area == Area.MATE }.grid.columns.map { it.id })
            .containsExactly(Competency.idOf(Area.MATE, 1))
    }

    @Test
    fun `an area with no worked competency is not a table`() = runTest {
        seed()
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(SECTION_ID, PERIOD_ID).first()

        assertThat(summary.areas.map { it.area }).containsExactly(Area.PPSS)
    }

    @Test
    fun `a competency never marked worked is excluded from its area's columns`() = runTest {
        seed()
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(SECTION_ID, PERIOD_ID).first()

        assertThat(summary.areas.single().grid.columns.map { it.id })
            .containsExactly(Competency.idOf(Area.PPSS, 1))
    }

    @Test
    fun `a hidden area drops its table entirely`() = runTest {
        seed()
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), isWorked = true)
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.MATE, 1), isWorked = true)
        sectionAreaRepository.setAreaHidden(SECTION_ID, Area.MATE, isHidden = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")

        val summary: PeriodLevelSummary = getSummary(SECTION_ID, PERIOD_ID).first()

        assertThat(summary.areas.map { it.area }).containsExactly(Area.PPSS)
    }

    @Test
    fun `each area table has one row per active student ordered by name`() = runTest {
        seed()
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "BAUTISTA QUISPE, Jose")
        addStudent("student-2", "ACOSTA RIVERA, Luz")
        addStudent("student-3", "DELGADO HUAMAN, Pedro", isWithdrawn = true)

        val summary: PeriodLevelSummary = getSummary(SECTION_ID, PERIOD_ID).first()

        assertThat(summary.areas.single().grid.rows.map { it.student.id })
            .containsExactly("student-2", "student-1").inOrder()
    }

    @Test
    fun `a recorded level appears in its student and competency cell`() = runTest {
        seed()
        setCompetencyWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), isWorked = true)
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        savePeriodLevel(
            PeriodLevel(keyOf("student-1", Competency.idOf(Area.PPSS, 1))).withAchievementLevel(AchievementLevel.AD),
        )

        val summary: PeriodLevelSummary = getSummary(SECTION_ID, PERIOD_ID).first()

        assertThat(summary.areas.single().grid.rows.single().cells.single().achievementLevel)
            .isEqualTo(AchievementLevel.AD)
    }

    private suspend fun seed() {
        SeedCurriculumUseCase(competencyRepository).invoke()
    }

    private suspend fun addStudent(id: String, fullName: String, isWithdrawn: Boolean = false) {
        studentRepository.save(
            Student(
                id = id,
                sectionId = SECTION_ID,
                code = StudentCode("1234567890123${id.last()}"),
                fullName = fullName,
                withdrawalDate = LocalDate.of(2026, 5, 1).takeIf { isWithdrawn },
            ),
        )
    }

    private fun keyOf(studentId: String, competencyId: String): PeriodLevelKey = PeriodLevelKey(
        sectionId = SECTION_ID,
        periodId = PERIOD_ID,
        studentId = studentId,
        competencyId = competencyId,
    )
}
