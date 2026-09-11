package com.emm.gema.core.domain.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ValidateBackupUseCaseTest {

    private val validate = ValidateBackupUseCase(supportedSchemaVersion = 3)

    @Test
    fun `a database written by this schema version is valid`() {
        val validation: BackupValidation = validate(sqliteContent(schemaVersion = 3))

        assertThat(validation).isEqualTo(BackupValidation.Valid(schemaVersion = 3))
    }

    @Test
    fun `a database written by an older schema version is valid`() {
        val validation: BackupValidation = validate(sqliteContent(schemaVersion = 1))

        assertThat(validation).isEqualTo(BackupValidation.Valid(schemaVersion = 1))
    }

    @Test
    fun `a database written by a newer app is refused`() {
        val validation: BackupValidation = validate(sqliteContent(schemaVersion = 9))

        assertThat(validation).isEqualTo(BackupValidation.FromANewerApp(schemaVersion = 9, supportedSchemaVersion = 3))
    }

    @Test
    fun `a file that is not a sqlite database is refused`() {
        val validation: BackupValidation = validate("not a database at all".toByteArray())

        assertThat(validation).isEqualTo(BackupValidation.NotABackup)
    }

    @Test
    fun `a file shorter than a sqlite header is refused`() {
        val validation: BackupValidation = validate(ByteArray(size = 8))

        assertThat(validation).isEqualTo(BackupValidation.NotABackup)
    }
}
