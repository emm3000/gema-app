package com.emm.gema.core.domain.student

import com.emm.gema.core.domain.id.IdGenerator
import com.emm.gema.core.domain.section.SectionId

class SaveStudentUseCase(
    private val repository: StudentRepository,
    private val idGenerator: IdGenerator,
) {

    suspend operator fun invoke(
        sectionId: SectionId,
        studentId: StudentId?,
        code: String,
        fullName: String,
    ): StudentSaveResult {
        val name: String = fullName.trim()
        if (name.isBlank()) return StudentSaveResult.BlankName

        val studentCode: StudentCode = StudentCode.orNull(code) ?: return StudentSaveResult.InvalidCode
        if (isTaken(sectionId, studentId, studentCode)) return StudentSaveResult.DuplicateCode

        val student: Student = existing(studentId)
            ?.copy(code = studentCode, fullName = name)
            ?: Student(
                id = StudentId(idGenerator.newId()),
                sectionId = sectionId,
                code = studentCode,
                fullName = name,
            )
        repository.save(student)
        return StudentSaveResult.Saved(student)
    }

    private suspend fun isTaken(sectionId: SectionId, studentId: StudentId?, code: StudentCode): Boolean {
        val owner: Student = repository.findByCode(sectionId, code) ?: return false
        return owner.id != studentId
    }

    private suspend fun existing(studentId: StudentId?): Student? {
        if (studentId == null) return null
        return requireNotNull(repository.findById(studentId)) { "There is no student ${studentId.value}" }
    }
}
