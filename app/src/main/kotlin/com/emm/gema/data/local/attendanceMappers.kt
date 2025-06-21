package com.emm.gema.data.local

import com.emm.gema.domain.attendance.Attendance
import com.emm.gema.domain.attendance.AttendanceStatus
import java.time.LocalDate

fun AttendanceEntity.toDomain() = Attendance(
    id = id,
    date = LocalDate.parse(date),
    status = AttendanceStatus.valueOf(status),
    studentId = studentId,
    courseId = courseId,
)

fun List<AttendanceEntity>.toDomain() = map<AttendanceEntity, Attendance>(AttendanceEntity::toDomain)