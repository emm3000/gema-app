package com.emm.gema.data.local

import com.emm.gema.domain.attendance.Attendance
import com.emm.gema.domain.attendance.AttendanceStatus
import com.emm.gema.domain.dashboard.AttendanceToday
import java.time.LocalDate

fun AttendanceEntity.toDomain() = Attendance(
    id = id,
    date = LocalDate.parse(date),
    status = AttendanceStatus.valueOf(status),
    studentId = studentId,
    courseId = courseId,
)

fun List<AttendanceEntity>.toAttendanceDomainList(): List<Attendance> = map(AttendanceEntity::toDomain)

fun AttendanceTodayEntity.toDomain() = AttendanceToday(
    courseId = courseId,
    courseName = courseName,
    totalStudents = totalStudents,
    presentToday = presentToday,
)

fun List<AttendanceTodayEntity>.toAttendanceTodayDomainList() = map(AttendanceTodayEntity::toDomain)