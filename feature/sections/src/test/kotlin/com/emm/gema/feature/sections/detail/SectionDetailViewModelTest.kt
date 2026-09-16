package com.emm.gema.feature.sections.detail

import app.cash.turbine.test
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.evaluation.GetMissingPeriodLevelCountUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.GetActivitiesUseCase
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionDetailExtrasUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.ImportedTemplate
import com.emm.gema.core.domain.siagie.ImportedTemplateKind
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.DateNameProvider
import com.emm.gema.core.theme.test.FakeDateNameProvider
import com.emm.gema.feature.sections.FakeActivityRepository
import com.emm.gema.feature.sections.FakeAttendanceRepository
import com.emm.gema.feature.sections.FakePeriodLevelRepository
import com.emm.gema.feature.sections.FakePeriodRepository
import com.emm.gema.feature.sections.FakeSchoolYearRepository
import com.emm.gema.feature.sections.FakeSectionAreaRepository
import com.emm.gema.feature.sections.FakeSectionRepository
import com.emm.gema.feature.sections.FakeSiagieImportStore
import com.emm.gema.feature.sections.FakeStudentRepository
import com.emm.gema.feature.sections.FakeWorkedCompetencyRepository
import com.emm.gema.feature.sections.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val today: LocalDate = LocalDate.of(2026, 6, 1)

class SectionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(sectionId, SchoolYearId("2026"), Grade.THIRD, "A")
    private val sectionRepository = FakeSectionRepository(listOf(section))
    private val studentRepository = FakeStudentRepository(
        listOf(
            student("student-1", "12345678901234"),
            student("student-2", "12345678901235", LocalDate.of(2026, 9, 4)),
        )
    )

    private val sectionAreaRepository = FakeSectionAreaRepository()
    private val workedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val periodLevelRepository = FakePeriodLevelRepository()
    private val schoolYear = SchoolYear(
        id = SchoolYearId("2026"),
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val period = Period(
        id = PeriodId("period-2"),
        schoolYearId = SchoolYearId("2026"),
        number = 2,
        startDate = LocalDate.of(2026, 5, 11),
        endDate = LocalDate.of(2026, 7, 24),
    )
    private val attendanceRepository = FakeAttendanceRepository()
    private val activityRepository = FakeActivityRepository()
    private val siagieImportStore = FakeSiagieImportStore()
    private val dateNames: DateNameProvider = FakeDateNameProvider()
    private val clock: Clock = Clock.fixed(
        today.atStartOfDay(ZoneId.of("America/Lima")).toInstant(),
        ZoneId.of("America/Lima"),
    )

    private fun viewModel(): SectionDetailViewModel = SectionDetailViewModel(
        sectionId = sectionId,
        getSection = GetSectionUseCase(sectionRepository),
        getStudents = GetStudentsUseCase(studentRepository),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        getCurrentPeriod = GetCurrentPeriodUseCase(FakePeriodRepository(listOf(period)), clock),
        getSectionDetailExtras = GetSectionDetailExtrasUseCase(
            getMissingPeriodLevelCount = GetMissingPeriodLevelCountUseCase(
                sectionAreaRepository = sectionAreaRepository,
                workedCompetencyRepository = workedCompetencyRepository,
                studentRepository = studentRepository,
                periodLevelRepository = periodLevelRepository,
            ),
            getActivities = GetActivitiesUseCase(activityRepository),
            siagieImportStore = siagieImportStore,
        ),
        getAttendanceDay = GetAttendanceDayUseCase(studentRepository, attendanceRepository),
        clock = clock,
        dateNames = dateNames,
    )

    @Test
    fun `the hub shows the section and how many students still attend it`() {
        val state: SectionDetailUiState = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3ro A")
        assertThat(state.studentCount).isEqualTo(1)
        assertThat(state.todayLabel).isEqualTo("HOY · LUNES 1 DE JUNIO")
    }

    @Test
    fun `the students row opens the student list of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.StudentsClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToStudents(sectionId))
        }
    }

    @Test
    fun `the areas row opens the area selection of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.AreasClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToSectionAreas(sectionId))
        }
    }

    @Test
    fun `the levels row opens the grid of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.PeriodLevelsClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToPeriodLevels(sectionId))
        }
    }

    @Test
    fun `the levels row counts the cells still missing in the current period`() = runTest {
        workedCompetencyRepository.setWorked(
            SectionId("section-1"),
            PeriodId("period-2"),
            Competency.idOf(Area.PPSS, 1),
            isWorked = true,
        )
        workedCompetencyRepository.setWorked(
            SectionId("section-1"),
            PeriodId("period-2"),
            Competency.idOf(Area.PPSS, 2),
            isWorked = true,
        )

        val viewModel: SectionDetailViewModel = viewModel()

        assertThat(viewModel.state.value.currentPeriodLabel).isEqualTo("II Bimestre")
        assertThat(viewModel.state.value.missingPeriodLevelCount).isEqualTo(2)
    }

    @Test
    fun `the template line shows once a SIAGIE grades template is stored`() = runTest {
        siagieImportStore.apply(
            students = emptyList(),
            template = ImportedTemplate(
                sectionId = sectionId,
                kind = ImportedTemplateKind.GRADES,
                fileName = "6 Primaria EBR.xlsx",
                content = byteArrayOf(1),
                importedAt = clock.instant(),
            ),
        )

        assertThat(viewModel().state.value.hasStoredTemplate).isTrue()
    }

    @Test
    fun `the activities row counts activities recorded in the current period`() = runTest {
        activityRepository.activities.value = listOf(
            Activity(
                id = ActivityId("activity-1"),
                sectionId = sectionId,
                periodId = PeriodId("period-2"),
                name = "Feria de ciencias",
                date = today,
                competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
            ),
        )

        assertThat(viewModel().state.value.activityCount).isEqualTo(1)
    }

    @Test
    fun `renaming opens the form of this section inside its school year`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.RenameClicked)

            val expected = SectionDetailUiEffect.NavigateToSectionForm(SchoolYearId("2026"), sectionId)
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `today is announced as untaken until the first student is recorded`() = runTest {
        assertThat(viewModel().state.value.todayAttendanceSummary).isEqualTo("Sin tomar")
        assertThat(viewModel().state.value.isTodayAttendanceTaken).isFalse()

        attendanceRepository.record(
            AttendanceRecord(sectionId, StudentId("student-1"), today, AttendanceStatus.ABSENT),
        )

        assertThat(viewModel().state.value.todayAttendanceSummary).isEqualTo("1 de 2 presentes")
        assertThat(viewModel().state.value.isTodayAttendanceTaken).isTrue()
    }

    @Test
    fun `the activities row opens the activity list of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.ActivitiesClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToActivities(sectionId))
        }
    }

    @Test
    fun `the export row opens the export flow of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.ExportClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToExport(sectionId))
        }
    }

    @Test
    fun `the back action leaves the hub`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateBack)
        }
    }

    @Test
    fun `the attendance row and the primary action both open today`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.TakeAttendanceClicked)
            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToAttendanceDay(sectionId, today))

            viewModel.onIntent(SectionDetailUiIntent.AttendanceClicked)
            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToAttendanceDay(sectionId, today))
        }
    }
}

private fun student(id: String, code: String, withdrawalDate: LocalDate? = null): Student = Student(
    id = StudentId(id),
    sectionId = sectionId,
    code = StudentCode(code),
    fullName = "ACOSTA RIVERA, Luz Maria",
    withdrawalDate = withdrawalDate,
)
