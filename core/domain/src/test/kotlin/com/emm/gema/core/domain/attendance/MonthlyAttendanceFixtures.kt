package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import java.time.LocalDate
import java.time.YearMonth

internal val monthlySectionId: SectionId = SectionId("section-1")
internal val september: YearMonth = YearMonth.of(2026, 9)

internal fun monthlyRecord(studentId: StudentId, date: LocalDate, status: AttendanceStatus): AttendanceRecord =
    AttendanceRecord(sectionId = monthlySectionId, studentId = studentId, date = date, status = status)

internal fun monthlyStudent(id: String, code: String, fullName: String): Student = Student(
    id = StudentId(id),
    sectionId = monthlySectionId,
    code = StudentCode(code),
    fullName = fullName,
)
