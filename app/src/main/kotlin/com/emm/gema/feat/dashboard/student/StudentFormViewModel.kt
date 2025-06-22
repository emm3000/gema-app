@file:OptIn(FlowPreview::class)

package com.emm.gema.feat.dashboard.student

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.student.CreateStudentInput
import com.emm.gema.domain.student.StudentCreator
import com.emm.gema.feat.dashboard.sexOptions
import com.emm.gema.feat.shared.normalizeErrorMessage
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

data class StudentFormUiState(
    val name: String = "",
    val email: String = "",
    val dni: String = "",
    val sex: String = sexOptions.firstOrNull().orEmpty(),
    val isFormValid: Boolean = false,
    val isSuccessful: Boolean = false,
    val error: String? = null
)

class StudentFormViewModel(
    private val studentCreator: StudentCreator,
    private val courseId: String,
) : ViewModel() {

    var state by mutableStateOf(StudentFormUiState())
        private set

    init {
        combine(
            snapshotFlow { state.name }.debounce(300L),
            snapshotFlow { state.email }.debounce(300L),
            snapshotFlow { state.dni }.debounce(300L),
            snapshotFlow { state.sex }.debounce(300L),
        ) {
            state = state.copy(isFormValid = it.all<String>(String::isNotEmpty))
        }.launchIn(viewModelScope)
    }

    fun dispatch(action: StudentFormAction) {
        when (action) {
            is StudentFormAction.DniChanged -> state = state.copy(dni = action.dni)
            is StudentFormAction.EmailChanged -> state = state.copy(email = action.email)
            is StudentFormAction.NameChanged -> state = state.copy(name = action.name)
            is StudentFormAction.SexChanged -> state = state.copy(sex = action.sex)
            StudentFormAction.Save -> trySaveStudent()
        }
    }

    private fun trySaveStudent() = viewModelScope.launch {
        saveStudent()
    }

    private suspend fun saveStudent() = try {
        val input: CreateStudentInput = createStudentInput()
        studentCreator.create(input)
        state = state.copy(isSuccessful = true)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        state = state.copy(error = normalizeErrorMessage(e))
    }

    private fun createStudentInput() = CreateStudentInput(
        fullName = state.name,
        email = state.email,
        dni = state.dni,
        gender = state.sex,
        courseId = courseId,
        birthDate = "-"
    )
}