package com.emm.gema.feature.sections.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.feature.sections.title
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
) : ViewModel() {

    private val _state: MutableStateFlow<SectionDetailUiState> = MutableStateFlow(SectionDetailUiState())
    val state: StateFlow<SectionDetailUiState> = _state.asStateFlow()

    private val _effects: Channel<SectionDetailUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SectionDetailUiEffect> = _effects.receiveAsFlow()

    private var section: Section? = null

    init {
        viewModelScope.launch {
            val stored: Section? = getSection(sectionId)
            section = stored
            _state.value = _state.value.copy(isLoading = false, sectionTitle = stored?.title().orEmpty())
        }
        viewModelScope.launch {
            getStudents(sectionId).collect { students: List<Student> ->
                _state.value = _state.value.copy(studentCount = students.count { !it.isWithdrawn })
            }
        }
    }

    fun onIntent(intent: SectionDetailUiIntent) {
        when (intent) {
            SectionDetailUiIntent.StudentsClicked -> emit(SectionDetailUiEffect.NavigateToStudents(sectionId))
            SectionDetailUiIntent.AreasClicked -> emit(SectionDetailUiEffect.NavigateToSectionAreas(sectionId))
            SectionDetailUiIntent.RenameClicked -> rename()
            SectionDetailUiIntent.BackClicked -> emit(SectionDetailUiEffect.NavigateBack)
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
