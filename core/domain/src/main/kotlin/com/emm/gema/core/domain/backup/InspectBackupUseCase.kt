package com.emm.gema.core.domain.backup

data class BackupInspection(
    val fileName: String,
    val validation: BackupValidation,
)

class InspectBackupUseCase(
    private val documents: BackupDocuments,
    private val validateBackup: ValidateBackupUseCase,
) {

    suspend operator fun invoke(uri: String): BackupInspection = BackupInspection(
        fileName = documents.nameOf(uri),
        validation = validateBackup(documents.readHeader(uri)),
    )
}
