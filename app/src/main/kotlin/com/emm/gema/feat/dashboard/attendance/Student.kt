package com.emm.gema.feat.dashboard.attendance

import com.emm.gema.data.network.attendance.AttendanceResponse
import com.emm.gema.domain.attendance.AttendanceStatus

data class Student(
    val id: String,
    val fullName: String,
    val attendanceStatus: AttendanceStatus,
)

fun AttendanceResponse.toStudent() = Student(
    id = student.id,
    fullName = student.fullName,
    attendanceStatus = if (status == "PRESENT") AttendanceStatus.Present else AttendanceStatus.Absent,
)
