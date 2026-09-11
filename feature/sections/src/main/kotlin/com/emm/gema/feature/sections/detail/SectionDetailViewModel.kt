package com.emm.gema.feature.sections.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.evaluation.GetMissingPeriodLevelCountUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.title
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SectionDetailViewModel(
    private val sectionId: String,
    private val getSection: GetSectionUseCase,
    private val getStudents: GetStudentsUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getCurrentPeriod: GetCurrentPeriodUseCase,
    private val getMissingPeriodLevelCount: GetMissingPeriodLevelCountUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<SectionDetailUiState> = MutableStateFlow(SectionDetailUiState())
    val state: StateFlow<SectionDetailUiState> = _state.asStateFlow()

    private val _effects: Channel<SectionDetailUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SectionDetailUiEffect> = _effects.receiveAsFlow()

    private var section: Section? = null

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch {
            getStudents(sectionId).collect { students: List<Student> ->
                _state.value = _state.value.copy(studentCount = students.count { !it.isWithdrawn })
            }
        }
    }

    fun onIntent(intent: SectionDetailUiIntent) {
        when (intent) {
            SectionDetailUiIntent.StudentsClicked -> emit(SectionDetailUiEffect.NavigateToStudents(sectionId))
            SectionDetailUiIntent.PeriodLevelsClicked -> emit(SectionDetailUiEffect.NavigateToPeriodLevels(sectionId))
            SectionDetailUiIntent.AreasClicked -> emit(SectionDetailUiEffect.NavigateToSectionAreas(sectionId))
            SectionDetailUiIntent.RenameClicked -> rename()
            SectionDetailUiIntent.BackClicked -> emit(SectionDetailUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val stored: Section? = getSection(sectionId)
        section = stored
        _state.value = _state.value.copy(isLoading = false, sectionTitle = stored?.title().orEmpty())

        val schoolYearId: String = stored?.schoolYearId ?: return
        val schoolYear: SchoolYear = getSchoolYear(schoolYearId) ?: return
        val currentPeriod: Period = getCurrentPeriod(schoolYearId) ?: return

        _state.value = _state.value.copy(
            currentPeriodLabel = schoolYear.periodKind.labelFor(currentPeriod.number),
        )

        getMissingPeriodLevelCount(sectionId = sectionId, periodId = currentPeriod.id).collect { missing: Int ->
            _state.value = _state.value.copy(missingPeriodLevelCount = missing)
        }
    }

    private fun rename() {
        val schoolYearId: String = section?.schoolYearId ?: return
        emit(SectionDetailUiEffect.NavigateToSectionForm(schoolYearId, sectionId))
    }

    private fun emit(effect: SectionDetailUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
