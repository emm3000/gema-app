package com.emm.gema.feat.dashboard.course

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.course.CourseCreator
import com.emm.gema.domain.course.model.CreateCourseInput
import com.emm.gema.feat.shared.normalizeErrorMessage
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CourseFormViewModel(private val courseCreator: CourseCreator) : ViewModel() {

    var state by mutableStateOf(CourseFormUiState())
        private set

    init {
        snapshotFlow { state }
            .debounce(400)
            .onEach {
                state = it.copy(isValidFields = it.courseName.isNotBlank())
            }.launchIn(viewModelScope)
    }

    fun onAction(action: CourseFormAction) {
        when (action) {
            is CourseFormAction.OnCourseNameChange -> state = state.copy(courseName = action.value)
            is CourseFormAction.OnGradeChange -> state = state.copy(grade = action.value)
            is CourseFormAction.OnLevelChange -> state = state.copy(level = action.value)
            is CourseFormAction.OnSectionChange -> state = state.copy(section = action.value)
            is CourseFormAction.OnShiftChange -> state = state.copy(shift = action.value)
            CourseFormAction.OnSave -> save()
            CourseFormAction.ClearErrorDialog -> state = state.copy(error = null)
        }
    }

    private fun save() = viewModelScope.launch { trySaveCourse() }

    private suspend fun trySaveCourse() = try {
        val input: CreateCourseInput = createCourseInput()
        courseCreator.create(input)
        state = CourseFormUiState(success = true)
    } catch (e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
        state = state.copy(error = normalizeErrorMessage(e))
    }

    private fun createCourseInput() = CreateCourseInput(
        name = state.courseName,
        grade = state.grade,
        section = state.section,
        level = state.level,
        shift = state.shift,
        academicYear = 2025,
    )
}