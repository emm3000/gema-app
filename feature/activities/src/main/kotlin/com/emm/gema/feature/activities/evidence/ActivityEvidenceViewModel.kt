package com.emm.gema.feature.activities.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.activity.Activity
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.activity.EvidenceLevel
import com.emm.gema.core.domain.activity.EvidenceLevelKey
import com.emm.gema.core.domain.activity.GetActivityUseCase
import com.emm.gema.core.domain.activity.GetEvidenceForActivityUseCase
import com.emm.gema.core.domain.activity.RecordEvidenceLevelUseCase
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.GetWorkedCompetenciesUseCase
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.schoolyear.GetPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.orderedByName
import com.emm.gema.core.theme.asDayMonthYear
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

class ActivityEvidenceViewModel(
    private val activityId: ActivityId,
    private val getActivity: GetActivityUseCase,
    private val getSection: GetSectionUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getPeriod: GetPeriodUseCase,
    private val getWorkedCompetencies: GetWorkedCompetenciesUseCase,
    private val getStudents: GetStudentsUseCase,
    private val getEvidenceForActivity: GetEvidenceForActivityUseCase,
    private val recordEvidenceLevel: RecordEvidenceLevelUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ActivityEvidenceUiState> = MutableStateFlow(ActivityEvidenceUiState())
    val state: StateFlow<ActivityEvidenceUiState> = _state.asStateFlow()

    private val _effects: Channel<ActivityEvidenceUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<ActivityEvidenceUiEffect> = _effects.receiveAsFlow()

    private val selectedCompetencyId: MutableStateFlow<CompetencyId?> = MutableStateFlow(null)
    private val loadedSectionId: MutableStateFlow<SectionId?> = MutableStateFlow(null)
    private var activity: Activity? = null

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch { observeRows() }
    }

    fun onIntent(intent: ActivityEvidenceUiIntent) {
        when (intent) {
            is ActivityEvidenceUiIntent.CompetencySelected -> select(intent.id)
            is ActivityEvidenceUiIntent.LevelSelected -> record(intent.studentId, intent.level)
            ActivityEvidenceUiIntent.EditActivityClicked -> {
                val loaded: Activity = activity ?: return
                emit(ActivityEvidenceUiEffect.NavigateToActivityForm(loaded.sectionId, loaded.id))
            }

            ActivityEvidenceUiIntent.BackClicked -> emit(ActivityEvidenceUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val loadedActivity: Activity = getActivity(activityId) ?: return
        val section: Section = getSection(loadedActivity.sectionId) ?: return
        val schoolYear: SchoolYear = getSchoolYear(section.schoolYearId) ?: return
        val period: Period = getPeriod(loadedActivity.periodId) ?: return
        val competencies: List<Competency> = getWorkedCompetencies(loadedActivity.sectionId, loadedActivity.periodId)
            .first()
            .filter { it.id in loadedActivity.competencyIds }
            .sortedBy { it.siagieOrdinal }

        activity = loadedActivity
        _state.value = _state.value.copy(
            isLoading = false,
            sectionId = loadedActivity.sectionId,
            activityName = loadedActivity.name,
            activityDateLabel = loadedActivity.date.asDayMonthYear(),
            periodLabel = schoolYear.periodKind.labelFor(period.number),
            competencies = competencies.map { it.toColumn() },
            selectedCompetencyId = competencies.firstOrNull()?.id,
        )
        selectedCompetencyId.value = competencies.firstOrNull()?.id
        loadedSectionId.value = loadedActivity.sectionId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeRows() {
        loadedSectionId
            .filterNotNull()
            .flatMapLatest { sectionId ->
                combine(
                    selectedCompetencyId,
                    getStudents(sectionId),
                    getEvidenceForActivity(activityId),
                ) { competencyId: CompetencyId?, students: List<Student>, evidence: List<EvidenceLevel> ->
                    val activeStudents: List<Student> = students.filterNot { it.isWithdrawn }.orderedByName()
                    val recorded: Map<StudentId, AchievementLevel> = evidence
                        .filter { it.key.competencyId == competencyId }
                        .associate { it.key.studentId to it.achievementLevel }

                    Triple(
                        activeStudents.map { EvidenceLevelRow(it.id, it.fullName, recorded[it.id]) },
                        recorded.size,
                        activeStudents.size,
                    )
                }
            }
            .collect { (rows, recordedCount, totalCount) ->
                _state.value = _state.value.copy(rows = rows, recordedCount = recordedCount, totalCount = totalCount)
            }
    }

    private fun select(competencyId: CompetencyId) {
        _state.value = _state.value.copy(selectedCompetencyId = competencyId)
        selectedCompetencyId.value = competencyId
    }

    private fun record(studentId: StudentId, level: AchievementLevel?) {
        val competencyId: CompetencyId = _state.value.selectedCompetencyId ?: return
        val key = EvidenceLevelKey(activityId = activityId, studentId = studentId, competencyId = competencyId)

        viewModelScope.launch {
            runCatching { recordEvidenceLevel(key, level) }
                .onFailure { emit(ActivityEvidenceUiEffect.ShowMessage(ActivityEvidenceMessage.RECORD_FAILED)) }
        }
    }

    private fun emit(effect: ActivityEvidenceUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

private fun Competency.toColumn(): CompetencyColumn =
    CompetencyColumn(id = id, label = "${area.name} ${siagieOrdinal.toString().padStart(2, '0')}")
