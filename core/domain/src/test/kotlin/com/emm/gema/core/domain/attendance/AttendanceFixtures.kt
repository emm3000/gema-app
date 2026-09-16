package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate
import java.time.YearMonth

internal val sectionId: SectionId = SectionId("section-1")
internal val september: YearMonth = YearMonth.of(2026, 9)

internal fun record(studentId: StudentId, date: LocalDate, status: AttendanceStatus): AttendanceRecord =
    AttendanceRecord(sectionId = sectionId, studentId = studentId, date = date, status = status)

internal fun student(id: String, code: String, fullName: String): Student = Student(
    id = StudentId(id),
    sectionId = sectionId,
    code = StudentCode(code),
    fullName = fullName,
)
