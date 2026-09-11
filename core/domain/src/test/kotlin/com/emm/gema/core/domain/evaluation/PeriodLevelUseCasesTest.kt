package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
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

class PeriodLevelUseCasesTest {

    private val competencyRepository: InMemoryCompetencyRepository = InMemoryCompetencyRepository()
    private val workedCompetencyRepository: InMemoryWorkedCompetencyRepository = InMemoryWorkedCompetencyRepository()
    private val studentRepository: InMemoryStudentRepository = InMemoryStudentRepository()
    private val sectionAreaRepository: InMemorySectionAreaRepository = InMemorySectionAreaRepository()
    private val periodLevelRepository: InMemoryPeriodLevelRepository = InMemoryPeriodLevelRepository()

    private val setCompetencyWorked: SetCompetencyWorkedUseCase =
        SetCompetencyWorkedUseCase(workedCompetencyRepository)
    private val getPeriodLevel: GetPeriodLevelUseCase = GetPeriodLevelUseCase(periodLevelRepository)
    private val savePeriodLevel: SavePeriodLevelUseCase = SavePeriodLevelUseCase(periodLevelRepository)
    private val getGrid: GetPeriodLevelGridUseCase = GetPeriodLevelGridUseCase(
        getPeriodCompetencies = GetPeriodCompetenciesUseCase(competencyRepository, workedCompetencyRepository),
        studentRepository = studentRepository,
        periodLevelRepository = periodLevelRepository,
    )
    private val getRecordedCounts: GetRecordedLevelCountsUseCase =
        GetRecordedLevelCountsUseCase(periodLevelRepository)
    private val getAreaRecordedCounts: GetAreaRecordedLevelCountsUseCase =
        GetAreaRecordedLevelCountsUseCase(periodLevelRepository)
    private val getPeriodLevelCount: GetPeriodLevelCountUseCase =
        GetPeriodLevelCountUseCase(periodLevelRepository)
    private val getMissingCount: GetMissingPeriodLevelCountUseCase = GetMissingPeriodLevelCountUseCase(
        sectionAreaRepository = sectionAreaRepository,
        workedCompetencyRepository = workedCompetencyRepository,
        studentRepository = studentRepository,
        periodLevelRepository = periodLevelRepository,
    )

    @Test
    fun `setting a level stores it`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))

        val stored: PeriodLevel = getPeriodLevel(keyOf("student-1", firstCompetency))

        assertThat(stored.achievementLevel).isEqualTo(AchievementLevel.A)
    }

    @Test
    fun `changing a level replaces the stored one`() = runTest {
        val key: PeriodLevelKey = keyOf("student-1", firstCompetency)
        savePeriodLevel(PeriodLevel(key).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(getPeriodLevel(key).withAchievementLevel(AchievementLevel.B))

        assertThat(getPeriodLevel(key).achievementLevel).isEqualTo(AchievementLevel.B)
        assertThat(periodLevelRepository.observeByPeriod(SECTION_ID, PERIOD_ID).first()).hasSize(1)
    }

    @Test
    fun `clearing a level with nothing left removes the row`() = runTest {
        val key: PeriodLevelKey = keyOf("student-1", firstCompetency)
        savePeriodLevel(PeriodLevel(key).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(getPeriodLevel(key).withAchievementLevel(null))

        assertThat(periodLevelRepository.observeByPeriod(SECTION_ID, PERIOD_ID).first()).isEmpty()
    }

    @Test
    fun `a descriptive conclusion survives a cleared level`() = runTest {
        val key: PeriodLevelKey = keyOf("student-1", firstCompetency)
        savePeriodLevel(
            PeriodLevel(key)
                .withAchievementLevel(AchievementLevel.C)
                .withDescriptiveConclusion("Necesita apoyo"),
        )
        savePeriodLevel(getPeriodLevel(key).withAchievementLevel(null))

        assertThat(getPeriodLevel(key).descriptiveConclusion).isEqualTo("Necesita apoyo")
    }

    @Test
    fun `saving a C without a conclusion succeeds and is flagged incomplete`() = runTest {
        val key: PeriodLevelKey = keyOf("student-1", firstCompetency)
        savePeriodLevel(PeriodLevel(key).withAchievementLevel(AchievementLevel.C))

        assertThat(getPeriodLevel(key).isIncomplete).isTrue()
    }

    @Test
    fun `the grid holds one cell per worked competency and active student`() = runTest {
        seedAndWork(firstCompetency, secondCompetency)
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        addStudent("student-2", "BAUTISTA QUISPE, Jose")

        val grid: PeriodLevelGrid = getGrid(SECTION_ID, PERIOD_ID, Area.PPSS).first()

        assertThat(grid.columns.map { it.id }).containsExactly(firstCompetency, secondCompetency).inOrder()
        assertThat(grid.rows).hasSize(2)
        assertThat(grid.rows.map { it.cells.size }).containsExactly(2, 2)
    }

    @Test
    fun `a withdrawn student leaves the grid`() = runTest {
        seedAndWork(firstCompetency)
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        addStudent("student-2", "BAUTISTA QUISPE, Jose", isWithdrawn = true)

        val grid: PeriodLevelGrid = getGrid(SECTION_ID, PERIOD_ID, Area.PPSS).first()

        assertThat(grid.rows.map { it.student.id }).containsExactly("student-1")
    }

    @Test
    fun `the missing count counts the empty cells of the grid`() = runTest {
        seedAndWork(firstCompetency, secondCompetency)
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        addStudent("student-2", "BAUTISTA QUISPE, Jose")
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.AD))

        val grid: PeriodLevelGrid = getGrid(SECTION_ID, PERIOD_ID, Area.PPSS).first()

        assertThat(grid.missingCount).isEqualTo(3)
    }

    @Test
    fun `an unworked comment counts as recorded`() = runTest {
        seedAndWork(firstCompetency)
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        savePeriodLevel(
            PeriodLevel(keyOf("student-1", firstCompetency)).withUnworkedComment(UnworkedComment.OTHER),
        )

        val grid: PeriodLevelGrid = getGrid(SECTION_ID, PERIOD_ID, Area.PPSS).first()

        assertThat(grid.missingCount).isEqualTo(0)
    }

    @Test
    fun `the grid counts the incomplete cells`() = runTest {
        seedAndWork(firstCompetency)
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.C))

        val grid: PeriodLevelGrid = getGrid(SECTION_ID, PERIOD_ID, Area.PPSS).first()

        assertThat(grid.incompleteCount).isEqualTo(1)
    }

    @Test
    fun `recorded counts are reported per competency`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(PeriodLevel(keyOf("student-2", firstCompetency)).withAchievementLevel(AchievementLevel.B))
        savePeriodLevel(PeriodLevel(keyOf("student-1", secondCompetency)).withAchievementLevel(AchievementLevel.C))

        val counts: Map<String, Int> = getRecordedCounts(SECTION_ID, PERIOD_ID).first()

        assertThat(counts[firstCompetency]).isEqualTo(2)
        assertThat(counts[secondCompetency]).isEqualTo(1)
    }

    @Test
    fun `a cell with only a descriptive conclusion is not recorded`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withDescriptiveConclusion("Pendiente"))

        assertThat(getRecordedCounts(SECTION_ID, PERIOD_ID).first()).isEmpty()
    }

    @Test
    fun `recorded counts are reported per area for the whole section`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(
            PeriodLevel(keyOf("student-1", Competency.idOf(Area.MATE, 1)))
                .withAchievementLevel(AchievementLevel.B),
        )

        val counts: Map<Area, Int> = getAreaRecordedCounts(SECTION_ID).first()

        assertThat(counts[Area.PPSS]).isEqualTo(1)
        assertThat(counts[Area.MATE]).isEqualTo(1)
    }

    @Test
    fun `the section level count adds every recorded level`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(PeriodLevel(keyOf("student-2", firstCompetency)).withAchievementLevel(AchievementLevel.A))

        assertThat(getPeriodLevelCount(SECTION_ID).first()).isEqualTo(2)
    }

    @Test
    fun `the missing count spans every visible area of the period`() = runTest {
        seedAndWork(firstCompetency, Competency.idOf(Area.MATE, 1))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        savePeriodLevel(PeriodLevel(keyOf("student-1", firstCompetency)).withAchievementLevel(AchievementLevel.A))

        assertThat(getMissingCount(SECTION_ID, PERIOD_ID).first()).isEqualTo(1)
    }

    @Test
    fun `a hidden area leaves the missing count`() = runTest {
        seedAndWork(firstCompetency, Competency.idOf(Area.MATE, 1))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        sectionAreaRepository.setAreaHidden(SECTION_ID, Area.MATE, isHidden = true)

        assertThat(getMissingCount(SECTION_ID, PERIOD_ID).first()).isEqualTo(1)
    }

    private suspend fun seedAndWork(vararg competencyIds: String) {
        SeedCurriculumUseCase(competencyRepository).invoke()
        competencyIds.forEach { setCompetencyWorked(SECTION_ID, PERIOD_ID, it, isWorked = true) }
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

    private val firstCompetency: String = Competency.idOf(Area.PPSS, 1)
    private val secondCompetency: String = Competency.idOf(Area.PPSS, 2)
}
