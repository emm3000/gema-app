package com.emm.gema.core.domain.siagie

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.student.StudentCode
import java.time.LocalDate
import java.time.YearMonth

data class AttendanceExportEntry(
    val studentCode: StudentCode,
    val statusesByDate: Map<LocalDate, AttendanceStatus>,
)

data class AttendanceExportFile(
    val fileName: String,
    val path: String,
)

interface MonthlyAttendanceExporter {

    suspend fun export(
        templateUri: String,
        month: YearMonth,
        entries: List<AttendanceExportEntry>,
    ): AttendanceExportFile
}
