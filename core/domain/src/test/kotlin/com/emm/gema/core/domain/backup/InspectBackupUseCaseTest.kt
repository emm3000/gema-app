package com.emm.gema.core.domain.backup

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InspectBackupUseCaseTest {

    @Test
    fun `inspecting a picked file reports its name and that it can be restored`() = runTest {
        val inspect = InspectBackupUseCase(
            documents = FakeBackupDocuments(
                name = "gema-20260910-1432.gema",
                content = sqliteContent(schemaVersion = 2),
            ),
            validateBackup = ValidateBackupUseCase(supportedSchemaVersion = 3),
        )

        val inspection: BackupInspection = inspect("content://picked")

        assertThat(inspection.fileName).isEqualTo("gema-20260910-1432.gema")
        assertThat(inspection.validation).isEqualTo(BackupValidation.Valid(schemaVersion = 2))
    }

    @Test
    fun `inspecting a file that is not a backup reports the refusal before anything is replaced`() = runTest {
        val inspect = InspectBackupUseCase(
            documents = FakeBackupDocuments(name = "notas.xlsx", content = "not a database".toByteArray()),
            validateBackup = ValidateBackupUseCase(supportedSchemaVersion = 3),
        )

        val inspection: BackupInspection = inspect("content://picked")

        assertThat(inspection.fileName).isEqualTo("notas.xlsx")
        assertThat(inspection.validation).isEqualTo(BackupValidation.NotABackup)
    }
}
