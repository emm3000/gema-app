package com.emm.gema.core.domain.student

sealed interface StudentSaveResult {

    data class Saved(val student: Student) : StudentSaveResult

    data object BlankName : StudentSaveResult

    data object InvalidCode : StudentSaveResult

    data object DuplicateCode : StudentSaveResult
}
