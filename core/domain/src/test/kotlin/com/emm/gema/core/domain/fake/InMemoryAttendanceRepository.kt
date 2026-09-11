package com.emm.gema.core.domain.fake

import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryAttendanceRepository(initial: List<AttendanceRecord> = emptyList()) : AttendanceRepository {

    val records: MutableStateFlow<List<AttendanceRecord>> = MutableStateFlow(initial)

    override fun observeBySectionAndDate(sectionId: SectionId, date: LocalDate): Flow<List<AttendanceRecord>> =
        records.map { stored -> stored.filter { it.sectionId == sectionId && it.date == date } }

    override fun observeBySectionAndMonth(sectionId: SectionId, month: YearMonth): Flow<List<AttendanceRecord>> =
        records.map { stored ->
            stored.filter { it.sectionId == sectionId && YearMonth.from(it.date) == month }
        }

    override suspend fun record(record: AttendanceRecord) {
        records.value = records.value
            .filterNot { it.studentId == record.studentId && it.date == record.date } + record
    }

    override suspend fun countRecordedDays(sectionId: SectionId): Int = records.value
        .filter { it.sectionId == sectionId }
        .distinctBy { it.date }
        .size

    override suspend fun deleteBySection(sectionId: SectionId) {
        records.value = records.value.filterNot { it.sectionId == sectionId }
    }
}
