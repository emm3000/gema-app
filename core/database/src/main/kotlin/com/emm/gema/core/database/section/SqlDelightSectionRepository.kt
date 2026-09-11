package com.emm.gema.core.database.section

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.database.SectionQueries
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightSectionRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SectionRepository {

    private val queries: SectionQueries = database.sectionQueries

    override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> =
        queries.selectBySchoolYear(schoolYearId.value)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() }.sortedWith(compareBy({ it.grade.number }, { it.name })) }

    override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> = queries.selectCountsBySchoolYear()
        .asFlow()
        .mapToList(dispatcher)
        .map { rows -> rows.associate { SchoolYearId(it.school_year_id) to it.section_count.toInt() } }

    override suspend fun findById(id: SectionId): Section? = withContext(dispatcher) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(section: Section): Unit = withContext(dispatcher) {
        queries.insert(
            id = section.id.value,
            school_year_id = section.schoolYearId.value,
            grade = section.grade.name,
            name = section.name,
        )
    }

    override suspend fun delete(id: SectionId): Unit = withContext(dispatcher) {
        queries.deleteById(id.value)
    }
}
