package com.emm.gema.core.database.attendance

import com.emm.gema.core.database.Attendance as AttendanceRow
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate

fun AttendanceRow.toDomain(): AttendanceRecord = AttendanceRecord(
    sectionId = SectionId(section_id),
    studentId = StudentId(student_id),
    date = LocalDate.parse(date),
    status = AttendanceStatus.valueOf(status),
)
