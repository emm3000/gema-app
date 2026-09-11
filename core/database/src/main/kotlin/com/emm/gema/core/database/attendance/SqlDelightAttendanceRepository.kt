package com.emm.gema.core.database.attendance

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.core.database.AttendanceQueries
import com.emm.gema.core.database.GemaDb
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class SqlDelightAttendanceRepository(
    database: GemaDb,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AttendanceRepository {

    private val queries: AttendanceQueries = database.attendanceQueries

    override fun observeBySectionAndDate(sectionId: String, date: LocalDate): Flow<List<AttendanceRecord>> =
        queries.selectBySectionAndDate(sectionId, date.toString())
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun record(record: AttendanceRecord): Unit = withContext(dispatcher) {
        queries.insert(
            section_id = record.sectionId,
            student_id = record.studentId,
            date = record.date.toString(),
            status = record.status.name,
        )
    }

    override suspend fun countRecordedDays(sectionId: String): Int = withContext(dispatcher) {
        queries.countRecordedDays(sectionId).executeAsOne().toInt()
    }

    override suspend fun deleteBySection(sectionId: String): Unit = withContext(dispatcher) {
        queries.deleteBySection(sectionId)
    }
}
