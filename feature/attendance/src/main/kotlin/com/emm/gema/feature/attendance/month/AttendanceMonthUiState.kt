package com.emm.gema.feature.attendance.month

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.student.StudentId
import java.time.YearMonth

data class AttendanceMonthUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val month: YearMonth? = null,
    val recordedDayCount: Int = 0,
    val rows: List<AttendanceMonthRow> = emptyList(),
    val canExport: Boolean = false,
    val exportUnavailableReason: String? = null,
    val isExporting: Boolean = false,
)

data class AttendanceMonthRow(
    val studentId: StudentId,
    val displayName: String,
    val countsByStatus: Map<AttendanceStatus, Int>,
)
