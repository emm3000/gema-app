package com.emm.gema.core.database.student

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.StudentQueries
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightStudentRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : StudentRepository {

    private val queries: StudentQueries = database.studentQueries

    override fun observeBySection(sectionId: String): Flow<List<Student>> =
        queries.selectBySection(sectionId)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<String, Int>> =
        queries.selectActiveCountsBySection()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.associate { it.section_id to it.student_count.toInt() } }

    override suspend fun findById(id: String): Student? = withContext(dispatcher) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun findByCode(sectionId: String, code: StudentCode): Student? = withContext(dispatcher) {
        queries.selectByCode(sectionId, code.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(student: Student): Unit = withContext(dispatcher) {
        queries.insert(
            id = student.id,
            section_id = student.sectionId,
            student_code = student.code.value,
            full_name = student.fullName,
            siagie_id = student.siagieId,
            withdrawal_date = student.withdrawalDate?.toString(),
        )
    }

    override suspend fun deleteBySection(sectionId: String): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId)
    }
}
