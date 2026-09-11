package com.emm.gema.core.domain.backup

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RestoreBackupUseCaseTest {

    private val store = FakeBackupStore()

    @Test
    fun `restoring a valid backup replaces the database`() = runTest {
        val restore: RestoreBackupUseCase = restoreOf(sqliteContent(schemaVersion = 1, body = "the year"))

        val result: RestoreResult = restore("content://picked")

        assertThat(result).isEqualTo(RestoreResult.Restored)
        assertThat(store.replacedWith).contains("the year")
    }

    @Test
    fun `restoring a file that is not a backup leaves the database untouched`() = runTest {
        val restore: RestoreBackupUseCase = restoreOf("a photo of the register".toByteArray())

        val result: RestoreResult = restore("content://picked")

        assertThat(result).isEqualTo(RestoreResult.Rejected(BackupValidation.NotABackup))
        assertThat(store.replacedWith).isNull()
    }

    @Test
    fun `restoring a backup from a newer app leaves the database untouched`() = runTest {
        val restore: RestoreBackupUseCase = restoreOf(sqliteContent(schemaVersion = 9))

        val result: RestoreResult = restore("content://picked")

        assertThat(result).isEqualTo(
            RestoreResult.Rejected(
                BackupValidation.FromANewerApp(schemaVersion = 9, supportedSchemaVersion = 3),
            ),
        )
        assertThat(store.replacedWith).isNull()
    }

    private fun restoreOf(content: ByteArray): RestoreBackupUseCase = RestoreBackupUseCase(
        documents = FakeBackupDocuments(name = "gema-20260910-1432.gema", content = content),
        store = store,
        validateBackup = ValidateBackupUseCase(supportedSchemaVersion = 3),
    )
}
