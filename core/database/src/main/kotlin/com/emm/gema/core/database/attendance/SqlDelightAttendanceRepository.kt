package com.emm.gema.core.database.attendance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.AttendanceQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightAttendanceRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AttendanceRepository {

    private val queries: AttendanceQueries = database.attendanceQueries

    override fun observeBySectionAndDate(sectionId: SectionId, date: LocalDate): Flow<List<AttendanceRecord>> =
        queries.selectBySectionAndDate(sectionId.value, date.toString())
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeBySectionAndMonth(sectionId: SectionId, month: YearMonth): Flow<List<AttendanceRecord>> =
        queries.selectBySectionAndDateRange(sectionId.value, month.atDay(1).toString(), month.atEndOfMonth().toString())
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun record(record: AttendanceRecord): Unit = withContext(dispatcher) {
        queries.insert(
            section_id = record.sectionId.value,
            student_id = record.studentId.value,
            date = record.date.toString(),
            status = record.status.name,
        )
    }

    override suspend fun countRecordedDays(sectionId: SectionId): Int = withContext(dispatcher) {
        queries.countRecordedDays(sectionId.value).executeAsOne().toInt()
    }

    override suspend fun deleteBySection(sectionId: SectionId): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId.value)
    }
}
