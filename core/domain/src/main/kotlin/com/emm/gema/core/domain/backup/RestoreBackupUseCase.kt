package com.emm.gema.core.domain.backup

sealed interface RestoreResult {

    data object Restored : RestoreResult

    data class Rejected(val validation: BackupValidation) : RestoreResult
}

class RestoreBackupUseCase(
    private val documents: BackupDocuments,
    private val store: BackupStore,
    private val validateBackup: ValidateBackupUseCase,
) {

    suspend operator fun invoke(uri: String): RestoreResult {
        val validation: BackupValidation = validateBackup(documents.readHeader(uri))
        if (validation !is BackupValidation.Valid) return RestoreResult.Rejected(validation)

        documents.openInput(uri).use { source -> store.replaceDatabase(source) }
        return RestoreResult.Restored
    }
}
