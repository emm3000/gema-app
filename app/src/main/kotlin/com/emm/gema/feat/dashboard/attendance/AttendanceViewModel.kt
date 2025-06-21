package com.emm.gema.feat.dashboard.attendance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.data.network.attendance.AttendanceNetworkRepository
import com.emm.gema.data.network.attendance.AttendanceRequest
import com.emm.gema.data.network.attendance.AttendanceResponse
import com.emm.gema.data.network.attendance.StudentRequest
import com.emm.gema.data.network.course.CourseNetworkRepository
import com.emm.gema.data.network.course.CourseResponse
import com.emm.gema.feat.shared.normalizeErrorMessage
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface ScreenState {

    data class HttpException(val message: String) : ScreenState

    data class EmptyCourses(val message: String) : ScreenState

    data class EmptyStudents(val message: String) : ScreenState

    object Loading : ScreenState

    object None : ScreenState
}

data class AttendanceUiState(
    val courses: List<CourseResponse> = emptyList(),
    val courseSelected: CourseResponse? = null,
    val datePicker: LocalDate = LocalDate.now(),
    val attendance: List<Student> = emptyList(),
    val screenState: ScreenState = ScreenState.None,
)

class AttendanceViewModel(
    private val attendanceNetworkRepository: AttendanceNetworkRepository,
    private val courseNetworkRepository: CourseNetworkRepository,
) : ViewModel() {

    var state by mutableStateOf(AttendanceUiState())
        private set

    init {
        combine(
            snapshotFlow { state.datePicker },
            snapshotFlow { state.courseSelected }
        ) { datePicker, courseSelected ->
            if (courseSelected != null) {
                loadAttendance(courseSelected, datePicker.toString())
            }
        }.launchIn(viewModelScope)
        fetchCourses()
    }

    private fun fetchCourses() = viewModelScope.launch {
        try {
            state = state.copy(screenState = ScreenState.Loading)
            val coursesResponse = courseNetworkRepository.all()
            state = state.copy(
                courses = coursesResponse,
                screenState = if (coursesResponse.isEmpty()) ScreenState.EmptyCourses("No hay cursos disponibles") else ScreenState.None,
                courseSelected = coursesResponse.firstOrNull()
            )
        } catch (e: Exception) {
            state = state.copy(screenState = ScreenState.HttpException(normalizeErrorMessage(e)))
        }

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
                            student.copy(attendanceStatus = student.attendanceStatus.not())
                        } else {
                            student
                        }
                    })
            }

            AttendanceAction.RetryFetchStudents -> {
                state.courseSelected?.let { course ->
                    viewModelScope.launch {
                        loadAttendance(course, state.datePicker.toString())
                    }
                }
            }

            AttendanceAction.OnSave -> {
                viewModelScope.launch {
                    val attendanceRequest = AttendanceRequest(
                        date = state.datePicker.toString(),
                        courseId = state.courseSelected?.id ?: "",
                        students = state.attendance.map {
                            StudentRequest(
                                studentId = it.id,
                                status = it.attendanceStatus.forNetwork
                            )
                        }
                    )
                    attendanceNetworkRepository.create(attendanceRequest)
                }
            }
        }
    }

    private suspend fun loadAttendance(course: CourseResponse, date: String) {
        state = state.copy(screenState = ScreenState.Loading)

        try {
            val attendance = attendanceNetworkRepository.all(course.id, date)

            state = state.copy(
                attendance = attendance.map(AttendanceResponse::toStudent),
                screenState = if (attendance.isEmpty()) ScreenState.EmptyStudents("No hay estudiantes disponibles") else ScreenState.None
            )

        } catch (e: Exception) {
            state = state.copy(screenState = ScreenState.HttpException(normalizeErrorMessage(e)))
        }
    }

    fun attendance(courseId: String, date: String) = viewModelScope.launch {
        attendanceNetworkRepository.all(courseId, date)
    }
}