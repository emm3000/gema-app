package com.emm.gema.core.domain.student

class GetStudentUseCase(
    private val repository: StudentRepository,
) {

    suspend operator fun invoke(studentId: StudentId): Student? = repository.findById(studentId)
}
