package com.emm.gema.core.domain.backup

sealed interface BackupValidation {

    data class Valid(val schemaVersion: Int) : BackupValidation

    data object NotABackup : BackupValidation

    data class FromANewerApp(
        val schemaVersion: Int,
        val supportedSchemaVersion: Int,
    ) : BackupValidation
}
