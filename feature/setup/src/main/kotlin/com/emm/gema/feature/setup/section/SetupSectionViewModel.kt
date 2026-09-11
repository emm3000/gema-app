package com.emm.gema.feature.setup.section

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.setup.CompleteSetupUseCase
import com.emm.gema.core.domain.setup.CompletedSetup
import com.emm.gema.feature.setup.year.SchoolYearDraft
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val MISSING_NAME_ERROR: String = "Escribe el nombre de la sección"
private const val SAVE_FAILED_MESSAGE: String = "No se pudo guardar la configuración. Inténtalo otra vez."

class SetupSectionViewModel(
    private val draft: SchoolYearDraft,
    private val completeSetup: CompleteSetupUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<SetupSectionUiState> = MutableStateFlow(SetupSectionUiState())
    val state: StateFlow<SetupSectionUiState> = _state.asStateFlow()

    private val _effects: Channel<SetupSectionUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SetupSectionUiEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: SetupSectionUiIntent) {
        when (intent) {
            is SetupSectionUiIntent.GradeSelected -> update { it.copy(grade = intent.grade) }
            is SetupSectionUiIntent.SectionNameChanged -> update { it.copy(sectionName = intent.value) }
            SetupSectionUiIntent.FinishClicked -> save { SetupSectionUiEffect.NavigateToHome }
            SetupSectionUiIntent.AreaSelectionClicked -> save {
                SetupSectionUiEffect.NavigateToSectionAreas(it.section.id)
            }
            SetupSectionUiIntent.BackClicked -> emit(SetupSectionUiEffect.NavigateBack)
        }
    }

    private fun save(nextStep: (CompletedSetup) -> SetupSectionUiEffect) {
        val current: SetupSectionUiState = _state.value
        val grade: Grade = current.grade ?: return
        if (!current.canFinish || current.isSaving) return

        _state.value = current.copy(isSaving = true)
        viewModelScope.launch {
            runCatching {
                completeSetup(
                    yearLabel = draft.label,
                    startDate = draft.startDate,
                    endDate = draft.endDate,
                    periodKind = draft.periodKind,
                    grade = grade,
                    sectionName = current.sectionName,
                    periodDates = draft.periods,
                )
            }
                .onSuccess { _effects.send(nextStep(it)) }
                .onFailure { _effects.send(SetupSectionUiEffect.ShowMessage(SAVE_FAILED_MESSAGE)) }
            _state.value = _state.value.copy(isSaving = false)
        }
    }

    private fun update(change: (SetupSectionUiState) -> SetupSectionUiState) {
        _state.value = validate(change(_state.value))
    }

    private fun emit(effect: SetupSectionUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun validate(state: SetupSectionUiState): SetupSectionUiState {
        val sectionNameError: String? = MISSING_NAME_ERROR.takeIf { state.sectionName.isBlank() }
        return state.copy(
            sectionNameError = sectionNameError,
            canFinish = sectionNameError == null && state.grade != null,
        )
    }
}
