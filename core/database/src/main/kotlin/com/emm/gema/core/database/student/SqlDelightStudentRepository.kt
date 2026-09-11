package com.emm.gema.core.database.student

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.StudentQueries
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
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

    override fun observeBySection(sectionId: SectionId): Flow<List<Student>> =
        queries.selectBySection(sectionId.value)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() }.orderedByName() }

    override fun observeCountsBySection(): Flow<Map<SectionId, Int>> =
        queries.selectActiveCountsBySection()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.associate { SectionId(it.section_id) to it.student_count.toInt() } }

    override suspend fun listBySection(sectionId: SectionId): List<Student> = withContext(dispatcher) {
        queries.selectBySection(sectionId.value).executeAsList().map { it.toDomain() }.orderedByName()
    }

    override suspend fun findById(id: StudentId): Student? = withContext(dispatcher) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun findByCode(sectionId: SectionId, code: StudentCode): Student? = withContext(dispatcher) {
        queries.selectByCode(sectionId.value, code.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(student: Student): Unit = withContext(dispatcher) {
        queries.insert(
            id = student.id.value,
            section_id = student.sectionId.value,
            student_code = student.code.value,
            full_name = student.fullName,
            siagie_id = student.siagieId,
            withdrawal_date = student.withdrawalDate?.toString(),
        )
    }

    override suspend fun deleteBySection(sectionId: SectionId): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId.value)
    }
}
