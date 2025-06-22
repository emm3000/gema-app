package com.emm.gema.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.EvaluationQueries
import com.emm.gema.domain.evaluation.CreateEvaluationInput
import com.emm.gema.domain.evaluation.Evaluation
import com.emm.gema.domain.evaluation.EvaluationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

typealias EvaluationEntity = com.emm.gema.Evaluation

class DefaultEvaluationRepository(private val dao: EvaluationQueries): EvaluationRepository {

    override suspend fun create(evaluation: CreateEvaluationInput) = withContext(Dispatchers.IO) {
        dao.insert(
            id = UUID.randomUUID().toString(),
            name = evaluation.name,
            date = evaluation.date.toString(),
            type = evaluation.type,
            course_id = evaluation.courseId
        )
    }

    override fun findByCourseId(courseId: String): Flow<List<Evaluation>> = dao
        .findByCourse(courseId)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map(List<EvaluationEntity>::toDomain)
}