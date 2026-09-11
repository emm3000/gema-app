package com.emm.gema.feature.activities.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.GetActivitiesUseCase
import com.emm.gema.core.domain.activity.GetActivityEvidenceStudentCountsUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.GetWorkedCompetenciesUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.title
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val sectionId: SectionId,
    private val getSection: GetSectionUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getPeriods: GetPeriodsUseCase,
    private val getCurrentPeriod: GetCurrentPeriodUseCase,
    private val getActivities: GetActivitiesUseCase,
    private val getWorkedCompetencies: GetWorkedCompetenciesUseCase,
    private val getEvidenceStudentCounts: GetActivityEvidenceStudentCountsUseCase,
    private val getStudents: GetStudentsUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ActivitiesUiState> = MutableStateFlow(ActivitiesUiState())
    val state: StateFlow<ActivitiesUiState> = _state.asStateFlow()

    private val _effects: Channel<ActivitiesUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<ActivitiesUiEffect> = _effects.receiveAsFlow()

    private val selectedPeriodId: MutableStateFlow<PeriodId?> = MutableStateFlow(null)

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch { observeActivities() }
    }

    fun onIntent(intent: ActivitiesUiIntent) {
        when (intent) {
            is ActivitiesUiIntent.PeriodSelected -> select(intent.id)
            is ActivitiesUiIntent.ActivityClicked -> emit(ActivitiesUiEffect.NavigateToActivityEvidence(intent.id))
            ActivitiesUiIntent.AddActivityClicked ->
                emit(ActivitiesUiEffect.NavigateToActivityForm(sectionId, null))

            ActivitiesUiIntent.BackClicked -> emit(ActivitiesUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val section: Section = getSection(sectionId) ?: return
        val schoolYear: SchoolYear = getSchoolYear(section.schoolYearId) ?: return
        val periods: List<Period> = getPeriods(section.schoolYearId).first()
        val currentPeriod: Period? = getCurrentPeriod(section.schoolYearId)
        val selectedId: PeriodId? = (currentPeriod ?: periods.firstOrNull())?.id

        _state.value = _state.value.copy(
            isLoading = false,
            sectionTitle = section.title(),
            periods = periods.map {
                PeriodOption(
                    id = it.id,
                    label = schoolYear.periodKind.labelFor(it.number),
                    isCurrent = it.id == currentPeriod?.id,
                )
            },
            selectedPeriodId = selectedId,
        )
        selectedPeriodId.value = selectedId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeActivities() {
        selectedPeriodId
            .filterNotNull()
            .flatMapLatest { periodId ->
                combine(
                    getActivities(sectionId, periodId),
                    getWorkedCompetencies(sectionId, periodId),
                    getEvidenceStudentCounts(sectionId, periodId),
                    getStudents(sectionId),
                ) { activities: List<Activity>, competencies: List<Competency>, counts: Map<ActivityId, Int>,
                    students: List<Student> ->
                    val competenciesById: Map<CompetencyId, Competency> = competencies.associateBy { it.id }
                    val studentCount: Int = students.count { !it.isWithdrawn }

                    activities.map { activity ->
                        activity.toRow(competenciesById, counts[activity.id] ?: 0, studentCount)
                    }
                }
            }
            .collect { rows -> _state.value = _state.value.copy(activities = rows) }
    }

    private fun select(periodId: PeriodId) {
        _state.value = _state.value.copy(selectedPeriodId = periodId)
        selectedPeriodId.value = periodId
    }

    private fun emit(effect: ActivitiesUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

private fun Activity.toRow(
    competenciesById: Map<CompetencyId, Competency>,
    evidenceRecordedCount: Int,
    studentCount: Int,
): ActivityRow = ActivityRow(
    id = id,
    name = name,
    date = date,
    competencyLabels = competencyIds.mapNotNull { competenciesById[it]?.label() },
    evidenceRecordedCount = evidenceRecordedCount,
    studentCount = studentCount,
)

private fun Competency.label(): String = "${area.name} ${siagieOrdinal.toString().padStart(2, '0')}"
