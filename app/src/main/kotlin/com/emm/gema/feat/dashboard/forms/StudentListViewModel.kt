package com.emm.gema.feat.dashboard.forms

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.data.attendance.StudentResponse
import com.emm.gema.data.student.StudentRepository
import com.emm.gema.feat.shared.normalizeErrorMessage
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

data class StudentListUiState(
    val students: List<StudentResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StudentListViewModel(
    private val repository: StudentRepository,
    private val courseId: String,
): ViewModel() {

    var state by mutableStateOf(StudentListUiState())
        private set

    init {
        loadStudents()
    }

    fun loadStudents() = viewModelScope.launch {
        try {
            state = state.copy(isLoading = true)
            val students = repository.studentsByCourseId(courseId)
            state = state.copy(students = students, isLoading = false)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            state = state.copy(error = normalizeErrorMessage(e), isLoading = false)
        }
    }
}