package com.emm.gema.core.domain.evaluation

import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
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
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))

        val stored: PeriodLevel = getPeriodLevel(keyOf(firstStudentId, firstCompetency))

        assertThat(stored.achievementLevel).isEqualTo(AchievementLevel.A)
    }

    @Test
    fun `changing a level replaces the stored one`() = runTest {
        val key: PeriodLevelKey = keyOf(firstStudentId, firstCompetency)
        savePeriodLevel(PeriodLevel(key).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(getPeriodLevel(key).withAchievementLevel(AchievementLevel.B))

        assertThat(getPeriodLevel(key).achievementLevel).isEqualTo(AchievementLevel.B)
        assertThat(periodLevelRepository.observeByPeriod(sectionId, periodId).first()).hasSize(1)
    }

    @Test
    fun `clearing a level with nothing left removes the row`() = runTest {
        val key: PeriodLevelKey = keyOf(firstStudentId, firstCompetency)
        savePeriodLevel(PeriodLevel(key).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(getPeriodLevel(key).withAchievementLevel(null))

        assertThat(periodLevelRepository.observeByPeriod(sectionId, periodId).first()).isEmpty()
    }

    @Test
    fun `a descriptive conclusion survives a cleared level`() = runTest {
        val key: PeriodLevelKey = keyOf(firstStudentId, firstCompetency)
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
        val key: PeriodLevelKey = keyOf(firstStudentId, firstCompetency)
        savePeriodLevel(PeriodLevel(key).withAchievementLevel(AchievementLevel.C))

        assertThat(getPeriodLevel(key).isIncomplete).isTrue()
    }

    @Test
    fun `the grid holds one cell per worked competency and active student`() = runTest {
        seedAndWork(listOf(firstCompetency, secondCompetency))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        addStudent("student-2", "BAUTISTA QUISPE, Jose")

        val grid: PeriodLevelGrid = getGrid(sectionId, periodId, Area.PPSS).first()

        assertThat(grid.columns.map { it.id }).containsExactly(firstCompetency, secondCompetency).inOrder()
        assertThat(grid.rows).hasSize(2)
        assertThat(grid.rows.map { it.cells.size }).containsExactly(2, 2)
    }

    @Test
    fun `a withdrawn student leaves the grid`() = runTest {
        seedAndWork(listOf(firstCompetency))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        addStudent("student-2", "BAUTISTA QUISPE, Jose", isWithdrawn = true)

        val grid: PeriodLevelGrid = getGrid(sectionId, periodId, Area.PPSS).first()

        assertThat(grid.rows.map { it.student.id }).containsExactly(firstStudentId)
    }

    @Test
    fun `the missing count counts the empty cells of the grid`() = runTest {
        seedAndWork(listOf(firstCompetency, secondCompetency))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        addStudent("student-2", "BAUTISTA QUISPE, Jose")
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.AD))

        val grid: PeriodLevelGrid = getGrid(sectionId, periodId, Area.PPSS).first()

        assertThat(grid.missingCount).isEqualTo(3)
    }

    @Test
    fun `an unworked comment counts as recorded`() = runTest {
        seedAndWork(listOf(firstCompetency))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        savePeriodLevel(
            PeriodLevel(keyOf(firstStudentId, firstCompetency)).withUnworkedComment(UnworkedComment.OTHER),
        )

        val grid: PeriodLevelGrid = getGrid(sectionId, periodId, Area.PPSS).first()

        assertThat(grid.missingCount).isEqualTo(0)
    }

    @Test
    fun `the grid counts the incomplete cells`() = runTest {
        seedAndWork(listOf(firstCompetency))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.C))

        val grid: PeriodLevelGrid = getGrid(sectionId, periodId, Area.PPSS).first()

        assertThat(grid.incompleteCount).isEqualTo(1)
    }

    @Test
    fun `recorded counts are reported per competency`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(PeriodLevel(keyOf(secondStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.B))
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, secondCompetency)).withAchievementLevel(AchievementLevel.C))

        val counts: Map<CompetencyId, Int> = getRecordedCounts(sectionId, periodId).first()

        assertThat(counts[firstCompetency]).isEqualTo(2)
        assertThat(counts[secondCompetency]).isEqualTo(1)
    }

    @Test
    fun `a cell with only a descriptive conclusion is not recorded`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withDescriptiveConclusion("Pendiente"))

        assertThat(getRecordedCounts(sectionId, periodId).first()).isEmpty()
    }

    @Test
    fun `recorded counts are reported per area for the whole section`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(
            PeriodLevel(keyOf(firstStudentId, Competency.idOf(Area.MATE, 1)))
                .withAchievementLevel(AchievementLevel.B),
        )

        val counts: Map<Area, Int> = getAreaRecordedCounts(sectionId).first()

        assertThat(counts[Area.PPSS]).isEqualTo(1)
        assertThat(counts[Area.MATE]).isEqualTo(1)
    }

    @Test
    fun `the section level count adds every recorded level`() = runTest {
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))
        savePeriodLevel(PeriodLevel(keyOf(secondStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))

        assertThat(getPeriodLevelCount(sectionId).first()).isEqualTo(2)
    }

    @Test
    fun `the missing count spans every visible area of the period`() = runTest {
        seedAndWork(listOf(firstCompetency, Competency.idOf(Area.MATE, 1)))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        savePeriodLevel(PeriodLevel(keyOf(firstStudentId, firstCompetency)).withAchievementLevel(AchievementLevel.A))

        assertThat(getMissingCount(sectionId, periodId).first()).isEqualTo(1)
    }

    @Test
    fun `a hidden area leaves the missing count`() = runTest {
        seedAndWork(listOf(firstCompetency, Competency.idOf(Area.MATE, 1)))
        addStudent("student-1", "ACOSTA RIVERA, Luz")
        sectionAreaRepository.setAreaHidden(sectionId, Area.MATE, isHidden = true)

        assertThat(getMissingCount(sectionId, periodId).first()).isEqualTo(1)
    }

    private suspend fun seedAndWork(competencyIds: List<CompetencyId>) {
        SeedCurriculumUseCase(competencyRepository).invoke()
        competencyIds.forEach { setCompetencyWorked(sectionId, periodId, it, isWorked = true) }
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

    private val firstCompetency: CompetencyId = Competency.idOf(Area.PPSS, 1)
    private val secondCompetency: CompetencyId = Competency.idOf(Area.PPSS, 2)
}

private val firstStudentId: StudentId = StudentId("student-1")

private val secondStudentId: StudentId = StudentId("student-2")
