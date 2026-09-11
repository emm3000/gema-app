package com.emm.gema.core.domain.attendance

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface AttendanceRepository {

    fun observeBySectionAndDate(sectionId: String, date: LocalDate): Flow<List<AttendanceRecord>>

    fun observeBySectionAndMonth(sectionId: String, month: YearMonth): Flow<List<AttendanceRecord>>

    suspend fun record(record: AttendanceRecord)

    suspend fun countRecordedDays(sectionId: String): Int

    suspend fun deleteBySection(sectionId: String)
}
