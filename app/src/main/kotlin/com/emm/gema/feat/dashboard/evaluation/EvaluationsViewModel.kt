@file:OptIn(ExperimentalCoroutinesApi::class)

package com.emm.gema.feat.dashboard.evaluation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.course.CoursesFetcher
import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.evaluation.CourseEvaluationsFetcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class EvaluationsViewModel(
    private val coursesFetcher: CoursesFetcher,
    private val courseEvaluationFetcher: CourseEvaluationsFetcher,
) : ViewModel() {

    var state by mutableStateOf(EvaluationsUiState())
        private set

    init {
        snapshotFlow { state.courseSelected }
            .flatMapLatest { course ->
                val zz = course?.id ?: return@flatMapLatest flowOf(emptyList())
                courseEvaluationFetcher.fetch(zz)
            }
            .onEach { evaluations ->
                state = state.copy(
                    evaluations = evaluations,
                    screenState = if (evaluations.isEmpty()) {
                        ScreenState.EmptyEvaluations("No hay evaluaciones disponibles")
                    } else {
                        ScreenState.None
                    }
                )
            }
            .launchIn(viewModelScope)
        fetchCourses()
    }

    fun onCourseSelected(course: Course) {
        state = state.copy(courseSelected = course)
    }

    private fun fetchCourses() {
        coursesFetcher.fetchAll()
            .onEach { courses ->
                state = state.copy(
                    courses = courses,
                    screenState = if (courses.isEmpty()) {
                        ScreenState.EmptyCourses("No hay cursos disponibles")
                    } else {
                        ScreenState.None
                    },
                    courseSelected = courses.firstOrNull()
                )
            }.launchIn(viewModelScope)
    }
}