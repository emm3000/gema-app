package com.emm.gema.domain.attendance

import com.emm.gema.domain.student.Student
import com.emm.gema.domain.student.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class AttendanceStatusFetcher(
    private val repository: AttendanceRepository,
    private val studentRepository: StudentRepository,
) {

    fun fetch(courseId: String, date: LocalDate): Flow<List<StudentAttendanceStatus>> {

        val students: Flow<List<Student>> = studentRepository
            .findByCourseId(courseId)

        val attendances: Flow<List<Attendance>> = repository
            .selectByDateAndCourse(courseId, date)

        return combine(
            students,
            attendances
        ) { students, attendances ->
            val attendanceMap: Map<String, Attendance> = attendances.associateBy(Attendance::studentId)
            students.map { student -> mapToAttendanceStatus(attendanceMap, student) }
        }
    }

    private fun mapToAttendanceStatus(
        attendanceMap: Map<String, Attendance>,
        student: Student,
    ): StudentAttendanceStatus {
        val status = attendanceMap[student.id]?.status ?: AttendanceStatus.Absent
        return StudentAttendanceStatus(
            studentId = student.id,
            student = student,
            status = status,
        )
    }
}