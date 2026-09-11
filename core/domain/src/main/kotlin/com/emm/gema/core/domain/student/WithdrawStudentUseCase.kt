package com.emm.gema.core.domain.student

import java.time.LocalDate

class WithdrawStudentUseCase(
    private val repository: StudentRepository,
) {

    suspend operator fun invoke(studentId: String, withdrawalDate: LocalDate) {
        val student: Student = requireNotNull(repository.findById(studentId)) {
            "There is no student $studentId"
        }
        repository.save(student.copy(withdrawalDate = withdrawalDate))
    }
}
