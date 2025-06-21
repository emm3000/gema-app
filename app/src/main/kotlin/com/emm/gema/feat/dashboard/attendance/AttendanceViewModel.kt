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
import com.emm.gema.domain.attendance.StudentAttendanceStatus
import com.emm.gema.domain.course.CoursesFetcher
import com.emm.gema.domain.course.model.Course
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface ScreenState {

    data class EmptyCourses(val message: String) : ScreenState

    data class EmptyStudents(val message: String) : ScreenState

    object None : ScreenState
}

data class AttendanceUiState(
    val courses: List<Course> = emptyList(),
    val courseSelected: Course? = null,
    val datePicker: LocalDate = LocalDate.now(),
    val attendance: List<StudentAttendanceStatus> = emptyList(),
    val screenState: ScreenState = ScreenState.None,
)

class AttendanceViewModel(
    private val coursesFetcher: CoursesFetcher,
    private val attendanceStatusFetcher: AttendanceStatusFetcher,
    private val attendanceUpdater: AttendanceUpdater,
) : ViewModel() {

    var state by mutableStateOf(AttendanceUiState())
        private set

    init {
        combine(
            snapshotFlow { state.datePicker },
            snapshotFlow { state.courseSelected }
        ) { datePicker, courseSelected ->
            if (courseSelected != null) {
                loadAttendance(courseSelected, datePicker)
            }
        }.launchIn(viewModelScope)
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
                state = state.copy(
                    attendance = state.attendance.map { student ->
                        if (student == action.student) {
                            student.copy(status = student.status.not())
                        } else {
                            student
                        }
                    })
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
                }
            }
        }
    }

    private suspend fun loadAttendance(course: Course, date: LocalDate) {
        val attendanceStatuses: List<StudentAttendanceStatus> = attendanceStatusFetcher.fetch(course.id, date)
        state = state.copy(
            attendance = attendanceStatuses,
            screenState = if (attendanceStatuses.isEmpty()) ScreenState.EmptyStudents("No hay estudiantes disponibles") else ScreenState.None
        )
    }
}