package com.emm.gema.core.domain.backup

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class BackupReminderTest {

    private val today: Instant = Instant.parse("2026-09-10T19:32:00Z")
    private val clock: Clock = Clock.fixed(today, ZoneId.of("America/Lima"))

    @Test
    fun `a teacher who never backed up is reminded`() = runTest {
        val status: BackupStatus = statusOf(lastBackupAt = null, thresholdDays = 7)

        assertThat(status.daysSinceLastBackup).isNull()
        assertThat(status.isReminderDue).isTrue()
    }

    @Test
    fun `a backup younger than the threshold is not reminded`() = runTest {
        val status: BackupStatus = statusOf(lastBackupAt = daysAgo(6), thresholdDays = 7)

        assertThat(status.daysSinceLastBackup).isEqualTo(6)
        assertThat(status.isReminderDue).isFalse()
    }

    @Test
    fun `a backup as old as the threshold is reminded`() = runTest {
        val status: BackupStatus = statusOf(lastBackupAt = daysAgo(7), thresholdDays = 7)

        assertThat(status.daysSinceLastBackup).isEqualTo(7)
        assertThat(status.isReminderDue).isTrue()
    }

    @Test
    fun `raising the threshold silences a reminder that was due`() = runTest {
        val settings = FakeBackupSettingsRepository(lastBackupAt = daysAgo(9), reminderThresholdDays = 7)
        val observeStatus = ObserveBackupStatusUseCase(settings, clock)
        val setThreshold = SetReminderThresholdUseCase(settings)

        assertThat(observeStatus().first().isReminderDue).isTrue()
        setThreshold(days = 30)

        assertThat(observeStatus().first().isReminderDue).isFalse()
        assertThat(observeStatus().first().reminderThresholdDays).isEqualTo(30)
    }

    @Test
    fun `a threshold outside the accepted range is refused`() = runTest {
        val settings = FakeBackupSettingsRepository(reminderThresholdDays = 7)
        val setThreshold = SetReminderThresholdUseCase(settings)

        val result: ReminderThresholdResult = setThreshold(days = 0)

        assertThat(result).isEqualTo(
            ReminderThresholdResult.OutOfRange(
                minimumDays = MINIMUM_REMINDER_THRESHOLD_DAYS,
                maximumDays = MAXIMUM_REMINDER_THRESHOLD_DAYS,
            ),
        )
        assertThat(settings.observeSettings().first().reminderThresholdDays).isEqualTo(7)
    }

    private suspend fun statusOf(lastBackupAt: Instant?, thresholdDays: Int): BackupStatus {
        val settings = FakeBackupSettingsRepository(lastBackupAt = lastBackupAt, reminderThresholdDays = thresholdDays)
        return ObserveBackupStatusUseCase(settings, clock)().first()
    }

    private fun daysAgo(days: Long): Instant = today.minus(days, ChronoUnit.DAYS)
}
