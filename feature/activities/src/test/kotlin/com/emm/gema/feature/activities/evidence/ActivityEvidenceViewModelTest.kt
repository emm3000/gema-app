package com.emm.gema.feature.activities.evidence

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.GetActivityUseCase
import com.emm.gema.core.domain.activity.GetEvidenceForActivityUseCase
import com.emm.gema.core.domain.activity.RecordEvidenceLevelUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetWorkedCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.schoolyear.GetPeriodUseCase
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
import com.emm.gema.feature.activities.FakeActivityRepository
import com.emm.gema.feature.activities.FakeCompetencyRepository
import com.emm.gema.feature.activities.FakeEvidenceLevelRepository
import com.emm.gema.feature.activities.FakePeriodRepository
import com.emm.gema.feature.activities.FakeSchoolYearRepository
import com.emm.gema.feature.activities.FakeSectionAreaRepository
import com.emm.gema.feature.activities.FakeSectionRepository
import com.emm.gema.feature.activities.FakeStudentRepository
import com.emm.gema.feature.activities.FakeWorkedCompetencyRepository
import com.emm.gema.feature.activities.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

private const val SECTION_ID: String = "section-1"
private const val SCHOOL_YEAR_ID: String = "year-1"
private const val PERIOD_ID: String = "period-1"
private const val ACTIVITY_ID: String = "activity-1"

class ActivityEvidenceViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private val schoolYear: SchoolYear = SchoolYear(
        id = SCHOOL_YEAR_ID,
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val section: Section = Section(SECTION_ID, SCHOOL_YEAR_ID, Grade.THIRD, "A")
    private val period: Period =
        Period(PERIOD_ID, SCHOOL_YEAR_ID, 1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 10))
    private val activity: Activity = Activity(
        id = ACTIVITY_ID,
        sectionId = SECTION_ID,
        periodId = PERIOD_ID,
        name = "Debate del aula",
        date = LocalDate.of(2026, 3, 10),
        competencyIds = setOf(Competency.idOf(Area.PPSS, 1)),
    )

    private val activityRepository: FakeActivityRepository = FakeActivityRepository(listOf(activity))
    private val evidenceLevelRepository: FakeEvidenceLevelRepository = FakeEvidenceLevelRepository(activityRepository)
    private val workedCompetencyRepository: FakeWorkedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val competencyRepository: FakeCompetencyRepository = FakeCompetencyRepository()
    private val studentRepository: FakeStudentRepository = FakeStudentRepository(
        listOf(
            Student(id = "student-1", sectionId = SECTION_ID, code = StudentCode("12345678901231"), fullName = "A"),
            Student(id = "student-2", sectionId = SECTION_ID, code = StudentCode("12345678901232"), fullName = "B"),
        ),
    )

    @Test
    fun `evidence is optional per student`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityEvidenceViewModel = viewModel()
        viewModel.onIntent(ActivityEvidenceUiIntent.LevelSelected("student-1", AchievementLevel.B))

        val state: ActivityEvidenceUiState = viewModel.state.value
        assertThat(state.rows.find { it.studentId == "student-1" }?.level).isEqualTo(AchievementLevel.B)
        assertThat(state.rows.find { it.studentId == "student-2" }?.level).isNull()
        assertThat(state.recordedCount).isEqualTo(1)
        assertThat(state.totalCount).isEqualTo(2)
    }

    @Test
    fun `recording no level clears a previously recorded one`() = runTest {
        workedCompetencyRepository.setWorked(SECTION_ID, PERIOD_ID, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityEvidenceViewModel = viewModel()
        viewModel.onIntent(ActivityEvidenceUiIntent.LevelSelected("student-1", AchievementLevel.B))

        viewModel.onIntent(ActivityEvidenceUiIntent.LevelSelected("student-1", null))

        assertThat(evidenceLevelRepository.levels.value).isEmpty()
    }

    private fun viewModel(): ActivityEvidenceViewModel = ActivityEvidenceViewModel(
        activityId = ACTIVITY_ID,
        getActivity = GetActivityUseCase(activityRepository),
        getSection = GetSectionUseCase(FakeSectionRepository(listOf(section))),
        getSchoolYear = GetSchoolYearUseCase(FakeSchoolYearRepository(listOf(schoolYear))),
        getPeriod = GetPeriodUseCase(FakePeriodRepository(listOf(period))),
        getWorkedCompetencies = GetWorkedCompetenciesUseCase(
            competencyRepository,
            workedCompetencyRepository,
            FakeSectionAreaRepository(),
        ),
        getStudents = GetStudentsUseCase(studentRepository),
        getEvidenceForActivity = GetEvidenceForActivityUseCase(evidenceLevelRepository),
        recordEvidenceLevel = RecordEvidenceLevelUseCase(evidenceLevelRepository),
    )
}
