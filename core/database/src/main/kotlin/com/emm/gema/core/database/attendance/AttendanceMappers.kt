package com.emm.gema.core.database.attendance

import com.emm.gema.core.database.Attendance as AttendanceRow
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceStatus
import java.time.LocalDate

fun AttendanceRow.toDomain(): AttendanceRecord = AttendanceRecord(
    sectionId = section_id,
    studentId = student_id,
    date = LocalDate.parse(date),
    status = AttendanceStatus.valueOf(status),
)
