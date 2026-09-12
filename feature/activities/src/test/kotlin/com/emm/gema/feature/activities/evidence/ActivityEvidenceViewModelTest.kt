package com.emm.gema.feature.activities.evidence

import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceMark
import com.emm.gema.core.domain.activity.GetActivityUseCase
import com.emm.gema.core.domain.activity.GetEvidenceForActivityUseCase
import com.emm.gema.core.domain.activity.RecordEvidenceLevelUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.GetWorkedCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.schoolyear.GetPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
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
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val sectionId: SectionId = SectionId("section-1")
private val schoolYearId: SchoolYearId = SchoolYearId("year-1")
private val periodId: PeriodId = PeriodId("period-1")
private val activityId: ActivityId = ActivityId("activity-1")

class ActivityEvidenceViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private val schoolYear: SchoolYear = SchoolYear(
        id = schoolYearId,
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val section: Section = Section(sectionId, schoolYearId, Grade.THIRD, "A")
    private val period: Period =
        Period(periodId, schoolYearId, 1, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 10))
    private val activity: Activity = Activity(
        id = activityId,
        sectionId = sectionId,
        periodId = periodId,
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
            Student(
                id = StudentId("student-1"),
                sectionId = sectionId,
                code = StudentCode("12345678901231"),
                fullName = "A",
            ),
            Student(
                id = StudentId("student-2"),
                sectionId = sectionId,
                code = StudentCode("12345678901232"),
                fullName = "B",
            ),
        ),
    )

    @Test
    fun `competency column label shows the code and the competency name`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityEvidenceViewModel = viewModel()

        val state: ActivityEvidenceUiState = viewModel.state.value
        assertThat(state.competencies.first().label).isEqualTo("PPSS 01 · Construye su identidad")
    }

    @Test
    fun `evidence is optional per student`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityEvidenceViewModel = viewModel()
        viewModel.onIntent(
            ActivityEvidenceUiIntent.LevelSelected(StudentId("student-1"), EvidenceMark.Level(AchievementLevel.B)),
        )

        val state: ActivityEvidenceUiState = viewModel.state.value
        assertThat(state.rows.find { it.studentId == StudentId("student-1") }?.mark)
            .isEqualTo(EvidenceMark.Level(AchievementLevel.B))
        assertThat(state.rows.find { it.studentId == StudentId("student-2") }?.mark).isNull()
        assertThat(state.recordedCount).isEqualTo(1)
        assertThat(state.totalCount).isEqualTo(2)
    }

    @Test
    fun `recording no level clears a previously recorded one`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityEvidenceViewModel = viewModel()
        viewModel.onIntent(
            ActivityEvidenceUiIntent.LevelSelected(StudentId("student-1"), EvidenceMark.Level(AchievementLevel.B)),
        )

        viewModel.onIntent(ActivityEvidenceUiIntent.LevelSelected(StudentId("student-1"), null))

        assertThat(evidenceLevelRepository.levels.value).isEmpty()
    }

    @Test
    fun `marking a student with explicit no evidence tints nothing and keeps the row recorded`() = runTest {
        workedCompetencyRepository.setWorked(sectionId, periodId, Competency.idOf(Area.PPSS, 1), true)
        val viewModel: ActivityEvidenceViewModel = viewModel()

        viewModel.onIntent(
            ActivityEvidenceUiIntent.LevelSelected(StudentId("student-1"), EvidenceMark.NoEvidence),
        )

        val state: ActivityEvidenceUiState = viewModel.state.value
        assertThat(state.rows.find { it.studentId == StudentId("student-1") }?.mark)
            .isEqualTo(EvidenceMark.NoEvidence)
        assertThat(state.rows.find { it.studentId == StudentId("student-2") }?.mark).isNull()
        assertThat(state.recordedCount).isEqualTo(1)
    }

    private fun viewModel(): ActivityEvidenceViewModel = ActivityEvidenceViewModel(
        activityId = activityId,
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
