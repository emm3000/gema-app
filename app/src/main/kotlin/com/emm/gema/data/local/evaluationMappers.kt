package com.emm.gema.data.local

import com.emm.gema.domain.evaluation.Evaluation

fun EvaluationEntity.toDomain() = Evaluation(
    id = id,
    name = name,
    date = date,
    maxScore = maxScore,
    type = type,
    term = term.orEmpty(),
    courseId = courseId
)

fun List<EvaluationEntity>.toDomain() = map<EvaluationEntity, Evaluation>(EvaluationEntity::toDomain)