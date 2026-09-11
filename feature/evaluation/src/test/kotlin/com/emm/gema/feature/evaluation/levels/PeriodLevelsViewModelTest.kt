package com.emm.gema.feature.evaluation.levels

import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceRecord
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.GetPeriodCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.GetPeriodLevelGridUseCase
import com.emm.gema.core.domain.evaluation.GetPeriodLevelSheetContextUseCase
import com.emm.gema.core.domain.evaluation.SavePeriodLevelUseCase
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.evaluation.FakeCompetencyRepository
import com.emm.gema.feature.evaluation.FakeEvidenceLevelRepository
import com.emm.gema.feature.evaluation.FakePeriodLevelRepository
import com.emm.gema.feature.evaluation.FakePeriodRepository
import com.emm.gema.feature.evaluation.FakeSchoolYearRepository
import com.emm.gema.feature.evaluation.FakeSectionAreaRepository
import com.emm.gema.feature.evaluation.FakeSectionRepository
import com.emm.gema.feature.evaluation.FakeStudentRepository
import com.emm.gema.feature.evaluation.FakeWorkedCompetencyRepository
import com.emm.gema.feature.evaluation.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val periodId: PeriodId = PeriodId("period-2")
private val schoolYearId: SchoolYearId = SchoolYearId("year-1")

class PeriodLevelsViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private val competencyRepository: FakeCompetencyRepository = FakeCompetencyRepository()
    private val workedCompetencyRepository: FakeWorkedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val studentRepository: FakeStudentRepository = FakeStudentRepository(students)
    private val sectionAreaRepository: FakeSectionAreaRepository = FakeSectionAreaRepository()
    private val periodLevelRepository: FakePeriodLevelRepository = FakePeriodLevelRepository()

    @Test
    fun `the grid opens on the current period and the first active area`() = runTest {
        work(firstCompetency)
        work(secondCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        val state: PeriodLevelsUiState = viewModel.state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3° A")
        assertThat(state.selectedPeriodId).isEqualTo(periodId)
        assertThat(state.selectedArea).isEqualTo(Area.COMU)
        assertThat(state.periods.single { it.isCurrent }.label).isEqualTo("II Bimestre")
    }

    @Test
    fun `a hidden area never becomes a grid option`() = runTest {
        sectionAreaRepository.setAreaHidden(sectionId, Area.COMU, isHidden = true)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        assertThat(viewModel.state.value.areas.map { it.area }).doesNotContain(Area.COMU)
    }

    @Test
    fun `only worked competencies become columns`() = runTest {
        work(secondCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        assertThat(viewModel.state.value.columns.map { it.id }).containsExactly(secondCompetency)
        assertThat(viewModel.state.value.hasWorkedCompetencies).isTrue()
    }

    @Test
    fun `an area with no worked competency has no columns`() = runTest {
        val viewModel: PeriodLevelsViewModel = createViewModel()

        assertThat(viewModel.state.value.hasWorkedCompetencies).isFalse()
        assertThat(viewModel.state.value.rows.flatMap { it.cells }).isEmpty()
    }

    @Test
    fun `the missing count starts at one cell per student and column`() = runTest {
        work(firstCompetency)
        work(secondCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        assertThat(viewModel.state.value.missingCount).isEqualTo(4)
    }

    @Test
    fun `setting a level from the sheet drops the missing count`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(firstStudentId, firstCompetency)))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(AchievementLevel.AD))

        assertThat(viewModel.state.value.missingCount).isEqualTo(1)
        assertThat(viewModel.state.value.sheet?.achievementLevel).isEqualTo(AchievementLevel.AD)
    }

    @Test
    fun `a C from the sheet asks for a conclusion without blocking`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(firstStudentId, firstCompetency)))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(AchievementLevel.C))

        assertThat(viewModel.cell(firstStudentId, firstCompetency).isIncomplete).isTrue()
    }

    @Test
    fun `writing the conclusion clears the incomplete mark`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(firstStudentId, firstCompetency)))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(AchievementLevel.C))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetDescriptiveConclusionChanged("Necesita apoyo"))

        assertThat(viewModel.cell(firstStudentId, firstCompetency).isIncomplete).isFalse()
        assertThat(viewModel.cell(firstStudentId, firstCompetency).hasDescriptiveConclusion).isTrue()
    }

    @Test
    fun `an unworked comment replaces the achievement level`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(firstStudentId, firstCompetency)))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(AchievementLevel.B))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetUnworkedCommentSelected(UnworkedComment.OTHER))

        assertThat(viewModel.cell(firstStudentId, firstCompetency).achievementLevel).isNull()
        assertThat(viewModel.cell(firstStudentId, firstCompetency).unworkedComment).isEqualTo(UnworkedComment.OTHER)
    }

    @Test
    fun `clearing the level empties the cell again`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(firstStudentId, firstCompetency)))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(AchievementLevel.B))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(null))

        assertThat(viewModel.cell(firstStudentId, firstCompetency).isRecorded).isFalse()
        assertThat(viewModel.state.value.missingCount).isEqualTo(2)
    }

    @Test
    fun `the missing filter keeps only the rows with an empty cell`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()
        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(firstStudentId, firstCompetency)))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(AchievementLevel.A))

        viewModel.onIntent(PeriodLevelsUiIntent.MissingFilterToggled)

        assertThat(viewModel.state.value.visibleRows.map { it.studentId }).containsExactly(secondStudentId)
    }

    @Test
    fun `the empty state routes to the worked competencies of the selected area and period`() = runTest {
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.WorkedCompetenciesClicked)

        val effect: PeriodLevelsUiEffect = viewModel.effects.first()
        assertThat(effect).isEqualTo(PeriodLevelsUiEffect.NavigateToWorkedCompetencies(sectionId, periodId, Area.COMU))
    }

    @Test
    fun `a column header starts the fill column mode on the first student`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.EnterColumnMode(firstCompetency))

        val state: PeriodLevelsUiState = viewModel.state.value
        assertThat(state.columnMode?.competencyId).isEqualTo(firstCompetency)
        assertThat(state.columnMode?.currentStudentIndex).isEqualTo(0)
        assertThat(state.columnModeStudent?.studentId).isEqualTo(firstStudentId)
        assertThat(state.columnModeColumn?.name).isEqualTo("Se comunica oralmente en lengua materna")
    }

    @Test
    fun `picking a level in column mode records it and advances`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()
        viewModel.onIntent(PeriodLevelsUiIntent.EnterColumnMode(firstCompetency))

        viewModel.onIntent(PeriodLevelsUiIntent.PickLevelForCurrent(AchievementLevel.A))

        assertThat(viewModel.cell(firstStudentId, firstCompetency).achievementLevel).isEqualTo(AchievementLevel.A)
        assertThat(viewModel.state.value.columnModeStudent?.studentId).isEqualTo(secondStudentId)
        assertThat(viewModel.state.value.columnMode?.currentStudentIndex).isEqualTo(1)
    }

    @Test
    fun `the last student closes the fill column mode`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()
        viewModel.onIntent(PeriodLevelsUiIntent.EnterColumnMode(firstCompetency))

        viewModel.onIntent(PeriodLevelsUiIntent.PickLevelForCurrent(AchievementLevel.A))
        viewModel.onIntent(PeriodLevelsUiIntent.PickLevelForCurrent(AchievementLevel.B))

        assertThat(viewModel.state.value.columnMode).isNull()
        assertThat(viewModel.state.value.missingCount).isEqualTo(0)
    }

    @Test
    fun `no level in column mode records nothing and still advances`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()
        viewModel.onIntent(PeriodLevelsUiIntent.EnterColumnMode(firstCompetency))

        viewModel.onIntent(PeriodLevelsUiIntent.PickLevelForCurrent(null))

        assertThat(viewModel.cell(firstStudentId, firstCompetency).isRecorded).isFalse()
        assertThat(viewModel.state.value.columnModeStudent?.studentId).isEqualTo(secondStudentId)
    }

    @Test
    fun `tapping a cell leaves the fill column mode and opens the sheet`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()
        viewModel.onIntent(PeriodLevelsUiIntent.EnterColumnMode(firstCompetency))

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(secondStudentId, firstCompetency)))

        assertThat(viewModel.state.value.columnMode).isNull()
        assertThat(viewModel.state.value.sheet?.studentId).isEqualTo(secondStudentId)
    }

    @Test
    fun `opening a cell loads its evidence read-only`() = runTest {
        work(firstCompetency)
        val evidence = EvidenceRecord(
            activityId = ActivityId("activity-1"),
            activityName = "Debate del aula",
            date = LocalDate.of(2026, 6, 10),
            achievementLevel = AchievementLevel.B,
        )
        val viewModel: PeriodLevelsViewModel =
            createViewModel(evidenceLevelRepository = FakeEvidenceLevelRepository(listOf(evidence)))

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(secondStudentId, firstCompetency)))

        assertThat(viewModel.state.value.sheet?.evidence).containsExactly(
            EvidenceRow(ActivityId("activity-1"), "Debate del aula", LocalDate.of(2026, 6, 10), AchievementLevel.B),
        )
    }

    @Test
    fun `finishing the fill column mode closes it`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()
        viewModel.onIntent(PeriodLevelsUiIntent.EnterColumnMode(firstCompetency))

        viewModel.onIntent(PeriodLevelsUiIntent.ExitColumnMode)

        assertThat(viewModel.state.value.columnMode).isNull()
    }

    @Test
    fun `changing the area reloads the columns`() = runTest {
        work(firstCompetency)
        workedCompetencyRepository.setWorked(sectionId, periodId, CompetencyId(mathCompetency.value), isWorked = true)
        val viewModel: PeriodLevelsViewModel = createViewModel()

        viewModel.onIntent(PeriodLevelsUiIntent.AreaSelected(Area.MATE))

        assertThat(viewModel.state.value.columns.map { it.id }).containsExactly(mathCompetency)
    }

    @Test
    fun `a failed write reports a message`() = runTest {
        work(firstCompetency)
        val viewModel: PeriodLevelsViewModel = createViewModel()
        periodLevelRepository.failsOnce = true

        viewModel.onIntent(PeriodLevelsUiIntent.CellClicked(PeriodLevelCellKey(firstStudentId, firstCompetency)))
        viewModel.onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(AchievementLevel.A))

        assertThat(viewModel.effects.first())
            .isEqualTo(PeriodLevelsUiEffect.ShowMessage(PeriodLevelsMessage.SAVE_FAILED))
    }

    @Test
    fun `an incoming cell opens its area and its sheet`() = runTest {
        work(firstCompetency)
        workedCompetencyRepository.setWorked(sectionId, periodId, CompetencyId(mathCompetency.value), isWorked = true)

        val viewModel: PeriodLevelsViewModel = createViewModel(
            PeriodLevelCellKey(studentId = secondStudentId, competencyId = CompetencyId(mathCompetency.value)),
        )

        assertThat(viewModel.state.value.selectedArea).isEqualTo(Area.MATE)
        val sheet: PeriodLevelSheetUiState = requireNotNull(viewModel.state.value.sheet)
        assertThat(sheet.studentId).isEqualTo(secondStudentId)
        assertThat(sheet.competencyId).isEqualTo(mathCompetency)
    }

    @Test
    fun `the grid opens without a sheet when no cell comes in`() = runTest {
        work(firstCompetency)

        val viewModel: PeriodLevelsViewModel = createViewModel()

        assertThat(viewModel.state.value.sheet).isNull()
    }

    private suspend fun work(competencyId: CompetencyId) {
        workedCompetencyRepository.setWorked(sectionId, periodId, competencyId, isWorked = true)
    }

    private fun createViewModel(
        initialCell: PeriodLevelCellKey? = null,
        evidenceLevelRepository: FakeEvidenceLevelRepository = FakeEvidenceLevelRepository(),
    ): PeriodLevelsViewModel = PeriodLevelsViewModel(
        sectionId = sectionId,
        initialCell = initialCell,
        getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        getPeriods = GetPeriodsUseCase(FakePeriodRepository(periods)),
        getCurrentPeriod = GetCurrentPeriodUseCase(FakePeriodRepository(periods), clock),
        getSectionAreas = GetSectionAreasUseCase(sectionAreaRepository),
        getPeriodLevelGrid = GetPeriodLevelGridUseCase(
            getPeriodCompetencies = GetPeriodCompetenciesUseCase(competencyRepository, workedCompetencyRepository),
            studentRepository = studentRepository,
            periodLevelRepository = periodLevelRepository,
        ),
        getPeriodLevelSheetContext = GetPeriodLevelSheetContextUseCase(periodLevelRepository, evidenceLevelRepository),
        savePeriodLevel = SavePeriodLevelUseCase(periodLevelRepository),
    )

    private fun PeriodLevelsViewModel.cell(
        studentId: StudentId,
        competencyId: CompetencyId,
    ): PeriodLevelCell = state.value
        .rows
        .single { it.studentId == studentId }
        .cells
        .single { it.competencyId == competencyId }

    private val schoolYear: SchoolYear = SchoolYear(
        id = schoolYearId,
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val section: Section = Section(id = sectionId, schoolYearId = schoolYearId, grade = Grade.THIRD, name = "A")
    private val periods: List<Period> = listOf(
        Period(
            id = PeriodId("period-1"),
            schoolYearId = schoolYearId,
            number = 1,
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 5, 8),
        ),
        Period(
            id = periodId,
            schoolYearId = schoolYearId,
            number = 2,
            startDate = LocalDate.of(2026, 5, 11),
            endDate = LocalDate.of(2026, 7, 24),
        ),
    )
    private val clock: Clock = Clock.fixed(
        LocalDate.of(2026, 6, 1).atStartOfDay(ZoneId.of("America/Lima")).toInstant(),
        ZoneId.of("America/Lima"),
    )
    private val firstCompetency: CompetencyId = Competency.idOf(Area.COMU, 1)
    private val secondCompetency: CompetencyId = Competency.idOf(Area.COMU, 2)
    private val mathCompetency: CompetencyId = Competency.idOf(Area.MATE, 1)
}

private val firstStudentId: StudentId = StudentId("student-1")

private val secondStudentId: StudentId = StudentId("student-2")

private val students: List<Student> = listOf(
    Student(
        id = firstStudentId,
        sectionId = sectionId,
        code = StudentCode("12345678901231"),
        fullName = "ACOSTA RIVERA, Luz Maria",
    ),
    Student(
        id = secondStudentId,
        sectionId = sectionId,
        code = StudentCode("12345678901232"),
        fullName = "BAUTISTA QUISPE, Jose",
    ),
)
