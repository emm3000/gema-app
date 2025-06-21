package com.emm.gema.feat.dashboard.course

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.data.network.course.CourseRepository
import com.emm.gema.data.network.course.CourseResponse
import com.emm.gema.feat.shared.normalizeErrorMessage
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {

    var state by mutableStateOf(CourseUiState())
        private set

    init {
        fetchCourses()
    }

    fun fetchCourses() = viewModelScope.launch { tryFetchCourses() }

    private suspend fun tryFetchCourses() = try {
        state = state.copy(isLoading = true, error = null)
        val courseList: List<CourseResponse> = repository.all()
        state = state.copy(courses = courseList, isLoading = false)
    } catch (e: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(e)
        state = state.copy(error = normalizeErrorMessage(e), isLoading = false)
    }
}