package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class InMemoryAttendanceRepository(initial: List<AttendanceRecord> = emptyList()) : AttendanceRepository {

    val records: MutableStateFlow<List<AttendanceRecord>> = MutableStateFlow(initial)

    override fun observeBySectionAndDate(sectionId: String, date: LocalDate): Flow<List<AttendanceRecord>> =
        records.map { stored -> stored.filter { it.sectionId == sectionId && it.date == date } }

    override suspend fun record(record: AttendanceRecord) {
        records.value = records.value
            .filterNot { it.studentId == record.studentId && it.date == record.date } + record
    }

    override suspend fun countRecordedDays(sectionId: String): Int = records.value
        .filter { it.sectionId == sectionId }
        .distinctBy { it.date }
        .size

    override suspend fun deleteBySection(sectionId: String) {
        records.value = records.value.filterNot { it.sectionId == sectionId }
    }
}
