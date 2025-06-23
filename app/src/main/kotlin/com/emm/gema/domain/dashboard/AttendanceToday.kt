package com.emm.gema.domain.dashboard

data class AttendanceToday(
    val courseId: String,
    val courseName: String,
    val totalStudents: Long,
    val presentToday: Long,
) {

    val attendancePercentage: Float
        get() = presentToday / totalStudents.toFloat()
}