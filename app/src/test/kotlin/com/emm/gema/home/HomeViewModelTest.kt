package com.emm.gema.home

import app.cash.turbine.test
import com.emm.gema.core.domain.backup.BackupSettings
import com.emm.gema.core.domain.backup.BackupSettingsRepository
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val now: Instant = Instant.parse("2026-09-10T19:32:00Z")
    private val clock: Clock = Clock.fixed(now, ZoneId.of("America/Lima"))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a teacher who never backed up is reminded on home`() = runTest {
        val viewModel: HomeViewModel = homeWith(lastBackupAt = null, thresholdDays = 7)

        val reminder: BackupReminder? = viewModel.state.value.backupReminder

        assertThat(reminder).isNotNull()
        assertThat(reminder?.hasEverBackedUp).isFalse()
    }

    @Test
    fun `a backup older than the threshold is reminded on home`() = runTest {
        val viewModel: HomeViewModel = homeWith(lastBackupAt = daysAgo(12), thresholdDays = 7)

        val reminder: BackupReminder? = viewModel.state.value.backupReminder

        assertThat(reminder?.daysSinceLastBackup).isEqualTo(12)
        assertThat(reminder?.hasEverBackedUp).isTrue()
    }

    @Test
    fun `a recent backup shows no reminder on home`() = runTest {
        val viewModel: HomeViewModel = homeWith(lastBackupAt = daysAgo(2), thresholdDays = 7)

        assertThat(viewModel.state.value.backupReminder).isNull()
    }

    @Test
    fun `tapping the reminder opens the backup screen`() = runTest {
        val viewModel: HomeViewModel = homeWith(lastBackupAt = null, thresholdDays = 7)

        viewModel.effects.test {
            viewModel.onIntent(HomeUiIntent.BackupReminderClicked)

            assertThat(awaitItem()).isEqualTo(HomeUiEffect.NavigateToBackup)
        }
    }

    private fun homeWith(lastBackupAt: Instant?, thresholdDays: Int): HomeViewModel {
        val settings = FakeBackupSettingsRepository(
            BackupSettings(lastBackupAt = lastBackupAt, reminderThresholdDays = thresholdDays),
        )
        return HomeViewModel(ObserveBackupStatusUseCase(settings, clock))
    }

    private fun daysAgo(days: Long): Instant = now.minus(days, ChronoUnit.DAYS)

    private class FakeBackupSettingsRepository(
        private val settings: BackupSettings,
    ) : BackupSettingsRepository {

        override fun observeSettings(): Flow<BackupSettings> = MutableStateFlow(settings)

        override suspend fun setLastBackupAt(instant: Instant) = Unit

        override suspend fun setReminderThresholdDays(days: Int) = Unit
    }
}
