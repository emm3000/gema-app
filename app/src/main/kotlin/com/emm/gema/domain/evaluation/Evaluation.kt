package com.emm.gema.domain.evaluation

data class Evaluation(
    val id: String,
    val name: String,
    val date: String,
    val maxScore: Long,
    val type: String,
    val term: String,
    val courseId: String,
)