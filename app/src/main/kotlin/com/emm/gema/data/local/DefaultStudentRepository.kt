package com.emm.gema.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.emm.gema.StudentCourseQueries
import com.emm.gema.StudentQueries
import com.emm.gema.domain.student.CreateStudentInput
import com.emm.gema.domain.student.Student
import com.emm.gema.domain.student.StudentId
import com.emm.gema.domain.student.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

typealias StudentEntity = com.emm.gema.Student

class DefaultStudentRepository(
    private val dao: StudentQueries,
    private val studentCourseDao: StudentCourseQueries,
) : StudentRepository {

    override suspend fun create(student: CreateStudentInput): StudentId {
        val newId: String = UUID.randomUUID().toString()
        dao.create(
            id = newId,
            fullName = student.fullName,
        )
        return StudentId(newId)
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
    }

    override fun all(): Flow<List<Student>> = dao
        .all()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map(List<StudentEntity>::toDomain)

    override fun findByCourseId(courseId: String): Flow<List<Student>> = studentCourseDao
        .studentsByCourseId(courseId)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map(List<StudentEntity>::toDomain)
}