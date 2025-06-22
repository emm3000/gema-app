package com.emm.gema.feat.dashboard.evaluation

import com.emm.gema.domain.course.model.Course
import java.time.LocalDate

sealed interface EvaluationFormAction {

    data class TitleChanged(val title: String) : EvaluationFormAction

    data class TypeChanged(val type: String) : EvaluationFormAction

    data class DateChanged(val date: LocalDate) : EvaluationFormAction

    data class CourseSelected(val course: Course) : EvaluationFormAction

    object Save : EvaluationFormAction
}