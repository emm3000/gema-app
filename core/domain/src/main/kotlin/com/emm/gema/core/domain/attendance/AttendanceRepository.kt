package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {

    fun observeBySectionAndDate(sectionId: SectionId, date: LocalDate): Flow<List<AttendanceRecord>>

    fun observeBySectionAndMonth(sectionId: SectionId, month: YearMonth): Flow<List<AttendanceRecord>>

    suspend fun record(record: AttendanceRecord)

    suspend fun countRecordedDays(sectionId: SectionId): Int

    suspend fun deleteBySection(sectionId: SectionId)
}
