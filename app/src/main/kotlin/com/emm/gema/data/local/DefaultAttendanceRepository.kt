package com.emm.gema.data.local

import com.emm.gema.AttendanceQueries
import com.emm.gema.domain.attendance.AttendanceRepository
import com.emm.gema.domain.attendance.CreateAttendanceInput
import java.util.UUID

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
}