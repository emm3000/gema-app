package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate

data class AttendanceRecord(
    val sectionId: SectionId,
    val studentId: StudentId,
    val date: LocalDate,
    val status: AttendanceStatus,
)
