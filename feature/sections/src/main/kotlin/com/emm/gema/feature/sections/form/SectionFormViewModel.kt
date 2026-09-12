package com.emm.gema.feature.sections.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.CreateSectionUseCase
import com.emm.gema.core.domain.section.DeleteSectionUseCase
import com.emm.gema.core.domain.section.GetSectionDeletionImpactUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionDeletionImpact
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.UpdateSectionUseCase
import com.emm.gema.core.domain.student.GetStudentCountsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SectionFormViewModel(
    private val schoolYearId: SchoolYearId,
    private val sectionId: SectionId?,
    private val getSection: GetSectionUseCase,
    private val createSection: CreateSectionUseCase,
    private val updateSection: UpdateSectionUseCase,
    private val deleteSection: DeleteSectionUseCase,
    private val getStudentCounts: GetStudentCountsUseCase,
    private val getSectionDeletionImpact: GetSectionDeletionImpactUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<SectionFormUiState> = MutableStateFlow(SectionFormUiState())
    val state: StateFlow<SectionFormUiState> = _state.asStateFlow()

    private val _effects: Channel<SectionFormUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SectionFormUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun onIntent(intent: SectionFormUiIntent) {
        when (intent) {
            is SectionFormUiIntent.GradeSelected -> update { it.copy(grade = intent.grade) }
            is SectionFormUiIntent.SectionNameChanged ->
                update { it.copy(sectionName = intent.value, sectionNameTouched = true) }
            SectionFormUiIntent.SaveClicked -> save()
            SectionFormUiIntent.DeleteClicked -> askForConfirmation()
            SectionFormUiIntent.DeleteConfirmed -> delete()
            SectionFormUiIntent.DeleteDismissed -> update { it.copy(deleteConfirmation = null) }
            SectionFormUiIntent.BackClicked -> emit(SectionFormUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val section: Section? = sectionId?.let { getSection(it) }
        val studentCount: Int = section?.let { getStudentCounts().first()[it.id] ?: 0 } ?: 0
        _state.value = validate(
            _state.value.copy(
                isLoading = false,
                sectionId = section?.id,
                grade = section?.grade,
                sectionName = section?.name.orEmpty(),
                canDelete = section != null,
                studentCount = studentCount,
            )
        )
    }

    private fun save() {
        update { it.copy(sectionNameTouched = true) }
        val current: SectionFormUiState = _state.value
        val grade: Grade = current.grade ?: return
        if (!current.canSave) return

        viewModelScope.launch {
            runCatching { persist(current.sectionId, grade, current.sectionName) }
                .onSuccess { _effects.send(SectionFormUiEffect.NavigateBack) }
                .onFailure { _effects.send(SectionFormUiEffect.ShowMessage(SectionFormMessage.SAVE_FAILED)) }
        }
    }

    private suspend fun persist(sectionId: SectionId?, grade: Grade, name: String) {
        if (sectionId == null) {
            createSection(schoolYearId = schoolYearId, grade = grade, name = name)
        } else {
            updateSection(sectionId = sectionId, grade = grade, name = name)
        }
    }

    private fun delete() {
        val sectionId: SectionId = _state.value.sectionId ?: return

        viewModelScope.launch {
            runCatching { deleteSection(sectionId) }
                .onSuccess { _effects.send(SectionFormUiEffect.NavigateBack) }
                .onFailure { _effects.send(SectionFormUiEffect.ShowMessage(SectionFormMessage.DELETE_FAILED)) }
            _state.value = _state.value.copy(deleteConfirmation = null)
        }
    }

    private fun askForConfirmation() {
        val sectionId: SectionId = _state.value.sectionId ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(deleteConfirmation = countWhatIsLost(sectionId))
        }
    }

    private suspend fun countWhatIsLost(sectionId: SectionId): DeleteConfirmation {
        val impact: SectionDeletionImpact = getSectionDeletionImpact(sectionId)
        return DeleteConfirmation(
            studentCount = impact.studentCount,
            attendanceDayCount = impact.attendanceDayCount,
            periodLevelCount = impact.periodLevelCount,
        )
    }

    private fun update(change: (SectionFormUiState) -> SectionFormUiState) {
        _state.value = validate(change(_state.value))
    }

    private fun emit(effect: SectionFormUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun validate(state: SectionFormUiState): SectionFormUiState {
        val sectionNameError: SectionFormMessage? =
            SectionFormMessage.MISSING_NAME.takeIf { state.sectionName.isBlank() }
        return state.copy(
            sectionNameError = sectionNameError.takeIf { !state.isLoading && state.sectionNameTouched },
            canSave = sectionNameError == null && state.grade != null,
        )
    }
}
