package com.emm.gema.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.StudentQueries
import com.emm.gema.domain.student.Student
import com.emm.gema.domain.student.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

typealias StudentEntity = com.emm.gema.Student

class DefaultStudentRepository(private val dao: StudentQueries) : StudentRepository {

    override suspend fun create(student: Student): Student {
        dao.create(
            id = student.id,
            full_name = student.fullName,
        )
        return student
    }

    override suspend fun update(
        studentId: String,
        student: Student
    ): Student {
        TODO("Not yet implemented")
    }

    override suspend fun find(studentId: String): Student? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(studentId: String) = withContext(Dispatchers.IO) {
        dao.delete(studentId)
        Unit
    }

    override suspend fun all(): Flow<List<Student>> = dao
        .all()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map(List<StudentEntity>::toDomain)
}