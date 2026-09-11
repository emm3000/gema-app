package com.emm.gema.core.domain.student

class GetStudentUseCase(
    private val repository: StudentRepository,
) {

    suspend operator fun invoke(studentId: String): Student? = repository.findById(studentId)
}
