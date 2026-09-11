package com.emm.gema.feature.students.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.GetStudentUseCase
import com.emm.gema.core.domain.student.ReactivateStudentUseCase
import com.emm.gema.core.domain.student.SaveStudentUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentSaveResult
import com.emm.gema.core.domain.student.WithdrawStudentUseCase
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StudentFormViewModel(
    private val sectionId: SectionId,
    private val studentId: StudentId?,
    private val getStudent: GetStudentUseCase,
    private val saveStudent: SaveStudentUseCase,
    private val withdrawStudent: WithdrawStudentUseCase,
    private val reactivateStudent: ReactivateStudentUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _state: MutableStateFlow<StudentFormUiState> = MutableStateFlow(StudentFormUiState())
    val state: StateFlow<StudentFormUiState> = _state.asStateFlow()

    private val _effects: Channel<StudentFormUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<StudentFormUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun onIntent(intent: StudentFormUiIntent) {
        when (intent) {
            is StudentFormUiIntent.StudentCodeChanged -> update { it.copy(studentCode = digitsOf(intent.value)) }
            is StudentFormUiIntent.FullNameChanged -> update { it.copy(fullName = intent.value) }
            is StudentFormUiIntent.WithdrawnToggled -> update { withdrawalOf(it, intent.isWithdrawn) }
            is StudentFormUiIntent.WithdrawalDateChanged -> update { it.copy(withdrawalDate = intent.value) }
            StudentFormUiIntent.SaveClicked -> save()
            StudentFormUiIntent.BackClicked -> emit(StudentFormUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val student: Student? = studentId?.let { getStudent(it) }
        _state.value = validate(
            _state.value.copy(
                isLoading = false,
                studentId = student?.id,
                studentCode = student?.code?.value.orEmpty(),
                fullName = student?.fullName.orEmpty(),
                isWithdrawn = student?.isWithdrawn == true,
                withdrawalDate = student?.withdrawalDate,
                hasSiagieId = student?.siagieId != null,
            )
        )
    }

    private fun save() {
        val current: StudentFormUiState = _state.value
        if (!current.canSave) return

        viewModelScope.launch {
            runCatching { persist(current) }
                .onSuccess { result -> onSaved(result) }
                .onFailure { _effects.send(StudentFormUiEffect.ShowMessage(StudentFormMessage.SAVE_FAILED)) }
        }
    }

    private suspend fun persist(state: StudentFormUiState): StudentSaveResult = saveStudent(
        sectionId = sectionId,
        studentId = state.studentId,
        code = state.studentCode,
        fullName = state.fullName,
    )

    private suspend fun onSaved(result: StudentSaveResult) {
        when (result) {
            is StudentSaveResult.Saved -> {
                applyWithdrawal(result.student)
                _effects.send(StudentFormUiEffect.NavigateBack)
            }
            StudentSaveResult.DuplicateCode -> _state.value = _state.value.copy(
                studentCodeError = StudentCodeError.DuplicateCode,
                canSave = false,
            )
            StudentSaveResult.InvalidCode, StudentSaveResult.BlankName ->
                _effects.send(StudentFormUiEffect.ShowMessage(StudentFormMessage.SAVE_FAILED))
        }
    }

    private suspend fun applyWithdrawal(student: Student) {
        val current: StudentFormUiState = _state.value
        val withdrawalDate: LocalDate? = current.withdrawalDate.takeIf { current.isWithdrawn }
        if (withdrawalDate == student.withdrawalDate) return
        if (withdrawalDate == null) {
            reactivateStudent(student.id)
        } else {
            withdrawStudent(student.id, withdrawalDate)
        }
    }

    private fun withdrawalOf(state: StudentFormUiState, isWithdrawn: Boolean): StudentFormUiState = state.copy(
        isWithdrawn = isWithdrawn,
        withdrawalDate = if (isWithdrawn) state.withdrawalDate ?: LocalDate.now(clock) else null,
    )

    private fun digitsOf(value: String): String = value.filter(Char::isDigit).take(StudentCode.LENGTH)

    private fun update(change: (StudentFormUiState) -> StudentFormUiState) {
        _state.value = validate(change(_state.value))
    }

    private fun emit(effect: StudentFormUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun validate(state: StudentFormUiState): StudentFormUiState {
        val studentCodeError: StudentCodeError? = StudentCodeError.InvalidLength(StudentCode.LENGTH)
            .takeIf { !StudentCode.isValid(state.studentCode) }
        val fullNameError: FullNameError? = FullNameError.BLANK.takeIf { state.fullName.isBlank() }
        val withdrawalDateError: WithdrawalDateError? = WithdrawalDateError.MISSING
            .takeIf { state.isWithdrawn && state.withdrawalDate == null }
        return state.copy(
            studentCodeError = studentCodeError.takeIf { !state.isLoading && state.studentCode.isNotEmpty() },
            studentCodeHint = "${state.studentCode.length} de ${StudentCode.LENGTH} dígitos",
            fullNameError = fullNameError.takeIf { !state.isLoading && state.fullName.isNotEmpty() },
            withdrawalDateError = withdrawalDateError.takeIf { !state.isLoading },
            canSave = studentCodeError == null && fullNameError == null && withdrawalDateError == null,
        )
    }
}
