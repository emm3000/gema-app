package com.emm.gema.core.domain.student

class ReactivateStudentUseCase(
    private val repository: StudentRepository,
) {

    suspend operator fun invoke(studentId: String) {
        val student: Student = requireNotNull(repository.findById(studentId)) {
            "There is no student $studentId"
        }
        repository.save(student.copy(withdrawalDate = null))
    }
}
