package com.emm.gema.core.domain.student

import java.time.LocalDate

class WithdrawStudentUseCase(
    private val repository: StudentRepository,
) {

    suspend operator fun invoke(studentId: StudentId, withdrawalDate: LocalDate) {
        val student: Student = requireNotNull(repository.findById(studentId)) {
            "There is no student ${studentId.value}"
        }
        repository.save(student.copy(withdrawalDate = withdrawalDate))
    }
}
