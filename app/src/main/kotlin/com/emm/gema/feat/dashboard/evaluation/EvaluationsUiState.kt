package com.emm.gema.feat.dashboard.evaluation

import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.evaluation.Evaluation

data class EvaluationsUiState(
    val courses: List<Course> = emptyList(),
    val courseSelected: Course? = null,
    val evaluations: List<Evaluation> = emptyList(),
    val screenState: ScreenState = ScreenState.None,
)