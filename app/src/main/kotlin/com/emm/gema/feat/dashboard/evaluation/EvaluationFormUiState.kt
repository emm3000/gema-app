package com.emm.gema.feat.dashboard.evaluation

import com.emm.gema.domain.course.model.Course
import com.emm.gema.feat.dashboard.examTypes
import java.time.LocalDate

data class EvaluationFormUiState(
    val courses: List<Course> = emptyList(),
    val selectedCourse: Course? = null,
    val title: String = "",
    val type: String = examTypes.first(),
    val date: LocalDate = LocalDate.now(),
)
