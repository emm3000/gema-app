package com.emm.gema.feature.attendance.month

import java.time.YearMonth

data class AttendanceMonthUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val month: YearMonth? = null,
    val monthLabel: String = "",
    val recordedDayCount: Int = 0,
    val rows: List<AttendanceMonthRow> = emptyList(),
    val canExport: Boolean = false,
    val exportUnavailableReason: String? = null,
    val isExporting: Boolean = false,
)

data class AttendanceMonthRow(
    val studentId: String,
    val displayName: String,
    val presentCount: Int,
    val lateCount: Int,
    val absentCount: Int,
    val justifiedCount: Int,
)
