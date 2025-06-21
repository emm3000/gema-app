package com.emm.gema.domain.student

import com.emm.gema.domain.course.CourseStudentAdder

class StudentCreator(
    private val studentRepository: StudentRepository,
    private val adder: CourseStudentAdder,
) {

    suspend fun create(student: CreateStudentInput) {
        val studentId: StudentId = studentRepository.create(student)
        adder.addStudent(student.courseId, studentId.value)
    }
}