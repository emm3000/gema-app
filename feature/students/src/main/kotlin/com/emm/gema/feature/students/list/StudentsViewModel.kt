package com.emm.gema.feature.students.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.title
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.ReactivateStudentUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val siagieMimeTypes: List<String> = listOf(
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.ms-excel",
)

class StudentsViewModel(
    private val sectionId: SectionId,
    private val getSection: GetSectionUseCase,
    private val getStudents: GetStudentsUseCase,
    private val reactivateStudent: ReactivateStudentUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<StudentsUiState> = MutableStateFlow(StudentsUiState())
    val state: StateFlow<StudentsUiState> = _state.asStateFlow()

    private val _effects: Channel<StudentsUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<StudentsUiEffect> = _effects.receiveAsFlow()

    private var students: List<Student> = emptyList()

    init {
        viewModelScope.launch {
            val section: Section? = getSection(sectionId)
            _state.value = _state.value.copy(sectionTitle = section?.title().orEmpty())
        }
        viewModelScope.launch {
            getStudents(sectionId).collect { stored ->
                students = stored
                _state.value = rowsOf(_state.value)
            }
        }
    }

    fun onIntent(intent: StudentsUiIntent) {
        when (intent) {
            is StudentsUiIntent.QueryChanged -> update { it.copy(query = intent.value) }
            is StudentsUiIntent.StudentClicked ->
                emit(StudentsUiEffect.NavigateToStudentForm(sectionId, intent.id))
            is StudentsUiIntent.ReactivateClicked -> reactivate(intent.id)
            is StudentsUiIntent.ImportFilePicked ->
                emit(StudentsUiEffect.NavigateToImportPreview(sectionId, intent.uri))
            StudentsUiIntent.ImportClicked -> emit(StudentsUiEffect.OpenDocumentPicker(siagieMimeTypes))
            StudentsUiIntent.AddStudentClicked ->
                emit(StudentsUiEffect.NavigateToStudentForm(sectionId, null))
            StudentsUiIntent.WithdrawnSectionToggled ->
                update { it.copy(isWithdrawnExpanded = !it.isWithdrawnExpanded) }
            StudentsUiIntent.BackClicked -> emit(StudentsUiEffect.NavigateBack)
        }
    }

    private fun reactivate(studentId: StudentId) {
        viewModelScope.launch {
            runCatching { reactivateStudent(studentId) }
                .onFailure { _effects.send(StudentsUiEffect.ShowMessage(StudentsMessage.REACTIVATE_FAILED)) }
        }
    }

    private fun update(change: (StudentsUiState) -> StudentsUiState) {
        _state.value = rowsOf(change(_state.value))
    }

    private fun rowsOf(state: StudentsUiState): StudentsUiState {
        val matching: List<Student> = students.filter { it.matches(state.query) }
        return state.copy(
            isLoading = false,
            activeStudents = matching.filterNot { it.isWithdrawn }.map { it.toRow() },
            withdrawnStudents = matching.filter { it.isWithdrawn }.map { it.toRow() },
        )
    }

    private fun emit(effect: StudentsUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun Student.matches(query: String): Boolean {
        val trimmed: String = query.trim()
        if (trimmed.isEmpty()) return true
        return fullName.contains(trimmed, ignoreCase = true) || code.value.contains(trimmed)
    }

    private fun Student.toRow(): StudentRow = StudentRow(
        id = id,
        displayName = fullName,
        studentCode = code.value,
        withdrawalDate = withdrawalDate,
    )
}
