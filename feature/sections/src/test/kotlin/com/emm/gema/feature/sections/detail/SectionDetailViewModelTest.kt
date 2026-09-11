package com.emm.gema.feature.sections.detail

import app.cash.turbine.test
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.evaluation.GetMissingPeriodLevelCountUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.feature.sections.FakePeriodLevelRepository
import com.emm.gema.feature.sections.FakePeriodRepository
import com.emm.gema.feature.sections.FakeSchoolYearRepository
import com.emm.gema.feature.sections.FakeSectionAreaRepository
import com.emm.gema.feature.sections.FakeSectionRepository
import com.emm.gema.feature.sections.FakeWorkedCompetencyRepository
import com.emm.gema.feature.sections.FakeStudentRepository
import com.emm.gema.feature.sections.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

private const val SECTION_ID: String = "section-1"

class SectionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val section = Section(SECTION_ID, "2026", Grade.THIRD, "A")
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
        id = "2026",
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val period = Period(
        id = "period-2",
        schoolYearId = "2026",
        number = 2,
        startDate = LocalDate.of(2026, 5, 11),
        endDate = LocalDate.of(2026, 7, 24),
    )
    private val clock: Clock = Clock.fixed(
        LocalDate.of(2026, 6, 1).atStartOfDay(ZoneId.of("America/Lima")).toInstant(),
        ZoneId.of("America/Lima"),
    )

    private fun viewModel(): SectionDetailViewModel = SectionDetailViewModel(
        sectionId = SECTION_ID,
        getSection = GetSectionUseCase(sectionRepository),
        getStudents = GetStudentsUseCase(studentRepository),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        getCurrentPeriod = GetCurrentPeriodUseCase(FakePeriodRepository(listOf(period)), clock),
        getMissingPeriodLevelCount = GetMissingPeriodLevelCountUseCase(
            sectionAreaRepository = sectionAreaRepository,
            workedCompetencyRepository = workedCompetencyRepository,
            studentRepository = studentRepository,
            periodLevelRepository = periodLevelRepository,
        ),
    )

    @Test
    fun `the hub shows the section and how many students still attend it`() {
        val state: SectionDetailUiState = viewModel().state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.sectionTitle).isEqualTo("3° A")
        assertThat(state.studentCount).isEqualTo(1)
    }

    @Test
    fun `the students row opens the student list of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.StudentsClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToStudents(SECTION_ID))
        }
    }

    @Test
    fun `the areas row opens the area selection of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.AreasClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToSectionAreas(SECTION_ID))
        }
    }

    @Test
    fun `the levels row opens the grid of this section`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.PeriodLevelsClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToPeriodLevels(SECTION_ID))
        }
    }

    @Test
    fun `the levels row counts the cells still missing in the current period`() = runTest {
        workedCompetencyRepository.setWorked("section-1", "period-2", Competency.idOf(Area.PPSS, 1), isWorked = true)
        workedCompetencyRepository.setWorked("section-1", "period-2", Competency.idOf(Area.PPSS, 2), isWorked = true)

        val viewModel: SectionDetailViewModel = viewModel()

        assertThat(viewModel.state.value.currentPeriodLabel).isEqualTo("II Bimestre")
        assertThat(viewModel.state.value.missingPeriodLevelCount).isEqualTo(2)
    }

    @Test
    fun `renaming opens the form of this section inside its school year`() = runTest {
        val viewModel: SectionDetailViewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SectionDetailUiIntent.RenameClicked)

            assertThat(awaitItem()).isEqualTo(SectionDetailUiEffect.NavigateToSectionForm("2026", SECTION_ID))
        }
    }
}

private fun student(id: String, code: String, withdrawalDate: LocalDate? = null): Student = Student(
    id = id,
    sectionId = SECTION_ID,
    code = StudentCode(code),
    fullName = "ACOSTA RIVERA, Luz Maria",
    withdrawalDate = withdrawalDate,
)
