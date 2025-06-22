package com.emm.gema.domain.evaluation

import kotlinx.coroutines.flow.Flow

class CourseEvaluationsFetcher(private val repository: EvaluationRepository) {

    fun fetch(courseId: String): Flow<List<Evaluation>> = repository.findByCourseId(courseId)
}