package com.emm.gema.domain.evaluation

import java.time.LocalDate

data class CreateEvaluationInput(
    val name: String,
    val date: LocalDate,
    val type: String,
    val courseId: String,
)