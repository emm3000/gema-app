package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetAttendanceDayUseCase(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
) {

    operator fun invoke(sectionId: SectionId, date: LocalDate): Flow<List<AttendanceEntry>> = combine(
        studentRepository.observeBySection(sectionId),
        attendanceRepository.observeBySectionAndDate(sectionId, date),
    ) { students: List<Student>, records: List<AttendanceRecord> ->
        students.filter { it.attends(date) }.map { student -> student.entryOf(records) }
    }

    private fun Student.attends(date: LocalDate): Boolean =
        withdrawalDate == null || date.isBefore(withdrawalDate)

    private fun Student.entryOf(records: List<AttendanceRecord>): AttendanceEntry {
        val record: AttendanceRecord? = records.find { it.studentId == id }
        return AttendanceEntry(
            student = this,
            status = record?.status ?: AttendanceStatus.PRESENT,
            isRecorded = record != null,
        )
    }
}
