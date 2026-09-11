package com.emm.gema.core.domain.student

class ReactivateStudentUseCase(
    private val repository: StudentRepository,
) {

    suspend operator fun invoke(studentId: StudentId) {
        val student: Student = requireNotNull(repository.findById(studentId)) {
            "There is no student ${studentId.value}"
        }
        repository.save(student.copy(withdrawalDate = null))
    }
}
