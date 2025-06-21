package com.emm.gema.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.AttendanceQueries
import com.emm.gema.domain.attendance.Attendance
import com.emm.gema.domain.attendance.AttendanceRepository
import com.emm.gema.domain.attendance.CreateAttendanceInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

typealias AttendanceEntity = com.emm.gema.Attendance

class DefaultAttendanceRepository(private val dao: AttendanceQueries) : AttendanceRepository {

    override suspend fun upsert(input: CreateAttendanceInput) {
        dao.transaction { upsertAttendance(input) }
    }

    private fun upsertAttendance(input: CreateAttendanceInput) {
        input.students.forEach {
            dao.upsertAttendace(
                id = UUID.randomUUID().toString(),
                date = input.date.toString(),
                status = it.status.name,
                studentId = it.studentId,
                courseId = input.courseId,
            )
        }
    }

    override fun selectByDateAndCourse(
        courseId: String,
        date: LocalDate,
    ): Flow<List<Attendance>> = dao.selectByDateAndCourse(courseId, date.toString())
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map(List<AttendanceEntity>::toDomain)
}