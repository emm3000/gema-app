package com.emm.gema.core.domain.attendance

import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.siagie.AttendanceExportEntry
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.core.domain.siagie.MonthlyAttendanceExporter
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentRepository
import java.time.YearMonth
import kotlinx.coroutines.flow.first

class ExportMonthlyAttendanceUseCase(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository,
    private val exporter: MonthlyAttendanceExporter,
) {

    suspend operator fun invoke(sectionId: SectionId, month: YearMonth, templateUri: String): AttendanceExportFile {
        val students: List<Student> = studentRepository.listBySection(sectionId)
        val records: List<AttendanceRecord> = attendanceRepository.observeBySectionAndMonth(sectionId, month).first()

        val entries: List<AttendanceExportEntry> = students.map { student ->
            AttendanceExportEntry(
                studentCode = student.code,
                statusesByDate = records
                    .filter { it.studentId == student.id }
                    .associate { it.date to it.status },
            )
        }

        return exporter.export(templateUri, month, entries)
    }
}
