package com.emm.gema.domain.evaluation

class EvaluationCreator(private val repository: EvaluationRepository) {

    suspend fun create(evaluation: CreateEvaluationInput) {
        repository.create(evaluation)
    }
}