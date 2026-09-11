package com.emm.gema.feature.attendance.day

import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate

data class AttendanceDayUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val date: LocalDate? = null,
    val canGoForward: Boolean = false,
    val presentCount: Int = 0,
    val totalCount: Int = 0,
    val unmarkedCount: Int = 0,
    val rows: List<AttendanceRow> = emptyList(),
) {
    val canMarkAllPresent: Boolean get() = unmarkedCount > 0
}

data class AttendanceRow(
    val studentId: StudentId,
    val displayName: String,
    val status: AttendanceStatus,
    val isRecorded: Boolean,
)
