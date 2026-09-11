package com.emm.gema.core.domain.attendance

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AttendanceRepository {

    fun observeBySectionAndDate(sectionId: String, date: LocalDate): Flow<List<AttendanceRecord>>

    suspend fun record(record: AttendanceRecord)

    suspend fun countRecordedDays(sectionId: String): Int

    suspend fun deleteBySection(sectionId: String)
}
