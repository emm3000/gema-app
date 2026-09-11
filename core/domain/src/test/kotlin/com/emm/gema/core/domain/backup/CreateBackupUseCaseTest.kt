package com.emm.gema.core.domain.backup

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CreateBackupUseCaseTest {

    private val createdAt: Instant = Instant.parse("2026-09-10T19:32:00Z")
    private val clock: Clock = Clock.fixed(createdAt, ZoneId.of("America/Lima"))
    private val store = FakeBackupStore()
    private val settings = FakeBackupSettingsRepository()
    private val createBackup = CreateBackupUseCase(store, settings, clock)

    @Test
    fun `a created backup is named after the local moment of creation`() = runTest {
        val file: BackupFile = createBackup()

        assertThat(file.name).isEqualTo("gema-20260910-1432.gema")
        assertThat(store.snapshotCount).isEqualTo(1)
    }

    @Test
    fun `creating a backup records when it happened`() = runTest {
        createBackup()

        assertThat(settings.observeSettings().first().lastBackupAt).isEqualTo(createdAt)
    }
}
