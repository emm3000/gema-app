package com.emm.gema.domain.evaluation

import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {

    suspend fun create(evaluation: CreateEvaluationInput)

    fun findByCourseId(courseId: String): Flow<List<Evaluation>>

    fun fetchAll(): Flow<List<Evaluation>>

    fun fetchSoon(): Flow<List<Evaluation>>
}