@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.emm.gema.feat.dashboard.attendance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.attendance.AttendanceStatusFetcher
import com.emm.gema.domain.attendance.AttendanceUpdater
import com.emm.gema.domain.attendance.CreateAttendanceInput
import com.emm.gema.domain.attendance.StudentAttendance
import com.emm.gema.domain.course.CoursesFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val coursesFetcher: CoursesFetcher,
    private val attendanceStatusFetcher: AttendanceStatusFetcher,
    private val attendanceUpdater: AttendanceUpdater,
) : ViewModel() {

    var state by mutableStateOf(AttendanceUiState())
        private set

    init {
        combine(
            snapshotFlow { state.courseSelected }.debounce(200L),
            snapshotFlow { state.datePicker }.debounce(200L),
            ::Pair
        )
            .flatMapLatest {
                attendanceStatusFetcher.fetch(it.first?.id.orEmpty(), it.second)
            }
            .onEach {
                state = state.copy(
                    attendance = it,
                    isSubmitButtonEnabled = false,
                    screenState = if (it.isEmpty()) ScreenState.EmptyStudents("No hay estudiantes disponibles") else ScreenState.None
                )
            }
            .launchIn(viewModelScope)
        fetchCourses()
    }

    private fun fetchCourses() {
        coursesFetcher.fetchAll()
            .onEach { courses ->
                state = state.copy(
                    courses = courses,
                    screenState = if (courses.isEmpty()) ScreenState.EmptyCourses("No hay cursos disponibles") else ScreenState.None,
                    courseSelected = courses.firstOrNull()
                )
            }.launchIn(viewModelScope)
    }

    fun onAction(action: AttendanceAction) {
        when (action) {
            is AttendanceAction.OnDateChange -> {
                state = state.copy(datePicker = action.date)
            }

            is AttendanceAction.OnCourseSelectedChange -> {
                state = state.copy(courseSelected = action.course)
            }

            is AttendanceAction.OnAttendanceStatusChange -> {
                updateAttendanceStatus(action)
            }

            AttendanceAction.OnSave -> {
                viewModelScope.launch {
                    val input = CreateAttendanceInput(
                        date = state.datePicker,
                        courseId = state.courseSelected?.id ?: "",
                        students = state.attendance.map {
                            StudentAttendance(
                                studentId = it.student.id,
                                status = it.status
                            )
                        }
                    )
                    attendanceUpdater.update(input)
                    state = state.copy(isSubmitButtonEnabled = false)
                }
            }
        }
    }

    private fun updateAttendanceStatus(action: AttendanceAction.OnAttendanceStatusChange) {
        state = state.copy(
            attendance = state.attendance.map { student ->
                if (student == action.student) {
                    student.copy(status = student.status.not())
                } else {
                    student
                }
            },
            isSubmitButtonEnabled = true
        )
    }
}