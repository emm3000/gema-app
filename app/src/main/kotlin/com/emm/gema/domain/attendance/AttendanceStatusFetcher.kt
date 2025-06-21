package com.emm.gema.domain.attendance

import com.emm.gema.domain.student.Student
import com.emm.gema.domain.student.StudentRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class AttendanceStatusFetcher(
    private val repository: AttendanceRepository,
    private val studentRepository: StudentRepository,
) {

    suspend fun fetch(courseId: String, date: LocalDate): List<StudentAttendanceStatus> {
        val students: List<Student> = studentRepository
            .findByCourseId(courseId)
            .firstOrNull()
            .orEmpty()
        val attendances: List<Attendance> = repository
            .selectByDateAndCourse(courseId, date)
            .firstOrNull()
            .orEmpty()

        val attendanceMap: Map<String, Attendance> = attendances.associateBy(Attendance::studentId)

        return students.map { student -> mapToAttendanceStatus(attendanceMap, student) }
    }

    private fun mapToAttendanceStatus(
        attendanceMap: Map<String, Attendance>,
        student: Student,
    ): StudentAttendanceStatus {
        val status = attendanceMap[student.id]?.status ?: AttendanceStatus.Absent
        return StudentAttendanceStatus(
            student = student,
            status = status,
        )
    }
}