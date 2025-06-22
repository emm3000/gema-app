package com.emm.gema.feat.dashboard.evaluation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.domain.course.CoursesFetcher
import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.evaluation.CreateEvaluationInput
import com.emm.gema.domain.evaluation.EvaluationCreator
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class EvaluationFormViewModel(
    private val evaluationCreator: EvaluationCreator,
    private val coursesFetcher: CoursesFetcher,
) : ViewModel() {

    var state by mutableStateOf(EvaluationFormUiState())
        private set

    init {
        fetchCourses()
    }

    private fun fetchCourses() = viewModelScope.launch {
        val courses: List<Course> = coursesFetcher.fetchAll().firstOrNull().orEmpty()
        state = state.copy(courses = courses, selectedCourse = courses.firstOrNull())
    }

    fun onAction(action: EvaluationFormAction) {
        when (action) {
            is EvaluationFormAction.CourseSelected -> state = state.copy(selectedCourse = action.course)
            is EvaluationFormAction.DateChanged -> state = state.copy(date = action.date)
            is EvaluationFormAction.TitleChanged -> state = state.copy(title = action.title)
            is EvaluationFormAction.TypeChanged -> state = state.copy(type = action.type)
            EvaluationFormAction.Save -> createEvaluation()
        }
    }

    private fun createEvaluation() = viewModelScope.launch {
        val input = CreateEvaluationInput(
            courseId = state.selectedCourse?.id.orEmpty(),
            name = state.title,
            type = state.type,
            date = state.date,
        )
        evaluationCreator.create(input)
    }
}