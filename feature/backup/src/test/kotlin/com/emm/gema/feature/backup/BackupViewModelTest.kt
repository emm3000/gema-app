package com.emm.gema.feature.backup

import app.cash.turbine.test
import com.emm.gema.core.domain.backup.CreateBackupUseCase
import com.emm.gema.core.domain.backup.InspectBackupUseCase
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import com.emm.gema.core.domain.backup.RestoreBackupUseCase
import com.emm.gema.core.domain.backup.SetReminderThresholdUseCase
import com.emm.gema.core.domain.backup.ValidateBackupUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearsUseCase
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.CountAllStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class BackupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now: Instant = Instant.parse("2026-09-10T19:32:00Z")
    private val clock: Clock = Clock.fixed(now, ZoneId.of("America/Lima"))
    private val store = FakeBackupStore()
    private val settings = FakeBackupSettingsRepository()
    private val schoolYears = FakeSchoolYearRepository(
        SchoolYear(
            id = SchoolYearId("the-year"),
            label = "2026",
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 12, 18),
            periodKind = PeriodKind.BIMESTER,
        ),
    )
    private val sections = FakeSectionRepository(
        Section(SectionId("section-1"), SchoolYearId("the-year"), Grade.FIRST, "A"),
    )
    private val students = FakeStudentRepository(
        aStudent("student-1", "12345678901234"),
        aStudent("student-2", "12345678901235"),
        aStudent("student-3", "12345678901236", withdrawalDate = LocalDate.of(2026, 9, 1)),
    )

    @Test
    fun `creating a backup hands the file to the share sheet`() = runTest {
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1))

        viewModel.effects.test {
            viewModel.onIntent(BackupUiIntent.CreateBackupClicked)

            val shared = awaitItem() as BackupUiEffect.ShareFile
            assertThat(shared.path).isEqualTo("/cache/backups/gema-20260910-1432.gema")
            assertThat(awaitItem()).isEqualTo(BackupUiEffect.ShowMessage(BackupMessage.BACKUP_CREATED))
        }
        assertThat(viewModel.state.value.isCreating).isFalse()
        assertThat(viewModel.state.value.daysSinceLastBackup).isEqualTo(0)
    }

    @Test
    fun `choosing a file to restore opens the document picker`() = runTest {
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1))

        viewModel.effects.test {
            viewModel.onIntent(BackupUiIntent.ChooseRestoreFileClicked)

            assertThat(awaitItem()).isInstanceOf(BackupUiEffect.OpenDocumentPicker::class.java)
        }
    }

    @Test
    fun `a picked backup is confirmed before anything is replaced`() = runTest {
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1))

        viewModel.onIntent(BackupUiIntent.RestoreFilePicked("content://picked"))

        val confirmation: RestoreConfirmation? = viewModel.state.value.restoreConfirmation
        assertThat(confirmation?.fileName).isEqualTo("gema-20260910-1432.gema")
        assertThat(confirmation?.currentSchoolYearCount).isEqualTo(1)
        assertThat(confirmation?.currentStudentCount).isEqualTo(3)
        assertThat(store.replacedDatabase).isFalse()
    }

    @Test
    fun `a picked file that is not a backup is refused without a confirmation`() = runTest {
        val viewModel: BackupViewModel = viewModelFor("a photo of the register".toByteArray())

        viewModel.effects.test {
            viewModel.onIntent(BackupUiIntent.RestoreFilePicked("content://picked"))

            assertThat(awaitItem())
                .isEqualTo(BackupUiEffect.ShowMessage(BackupMessage.FILE_IS_NOT_A_BACKUP))
        }
        assertThat(viewModel.state.value.restoreConfirmation).isNull()
        assertThat(store.replacedDatabase).isFalse()
    }

    @Test
    fun `a confirmed restore replaces the database and restarts the app`() = runTest {
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1))
        viewModel.onIntent(BackupUiIntent.RestoreFilePicked("content://picked"))

        viewModel.effects.test {
            viewModel.onIntent(BackupUiIntent.RestoreConfirmed)

            assertThat(awaitItem()).isEqualTo(BackupUiEffect.RestartApp)
        }
        assertThat(store.replacedDatabase).isTrue()
    }

    @Test
    fun `dismissing the confirmation leaves the database alone`() = runTest {
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1))
        viewModel.onIntent(BackupUiIntent.RestoreFilePicked("content://picked"))

        viewModel.onIntent(BackupUiIntent.RestoreDismissed)

        assertThat(viewModel.state.value.restoreConfirmation).isNull()
        assertThat(store.replacedDatabase).isFalse()
    }

    @Test
    fun `a reminder threshold inside the accepted range is saved`() = runTest {
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1))

        viewModel.onIntent(BackupUiIntent.ReminderThresholdChanged("30"))

        assertThat(viewModel.state.value.reminderThresholdDays).isEqualTo(30)
        assertThat(viewModel.state.value.isReminderThresholdInvalid).isFalse()
    }

    @Test
    fun `a reminder threshold outside the accepted range is reported and not saved`() = runTest {
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1))

        viewModel.onIntent(BackupUiIntent.ReminderThresholdChanged("0"))

        assertThat(viewModel.state.value.isReminderThresholdInvalid).isTrue()
        assertThat(viewModel.state.value.reminderThresholdDays).isEqualTo(7)
        assertThat(settings.observeSettings().first().reminderThresholdDays).isEqualTo(7)
    }

    @Test
    fun `a backup exactly at the reminder threshold is overdue`() = runTest {
        val reminderThresholdDays = 7
        val lastBackupAt: Instant = now.minus(reminderThresholdDays.toLong(), ChronoUnit.DAYS)
        val overdueSettings = FakeBackupSettingsRepository(
            lastBackupAt = lastBackupAt,
            reminderThresholdDays = reminderThresholdDays,
        )
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1), overdueSettings)

        assertThat(viewModel.state.value.isBackupOverdue).isTrue()
    }

    @Test
    fun `a backup one day below the reminder threshold is not overdue`() = runTest {
        val reminderThresholdDays = 7
        val lastBackupAt: Instant = now.minus((reminderThresholdDays - 1).toLong(), ChronoUnit.DAYS)
        val freshSettings = FakeBackupSettingsRepository(
            lastBackupAt = lastBackupAt,
            reminderThresholdDays = reminderThresholdDays,
        )
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1), freshSettings)

        assertThat(viewModel.state.value.isBackupOverdue).isFalse()
    }

    @Test
    fun `never having backed up is not reported as overdue`() = runTest {
        val neverBackedUpSettings = FakeBackupSettingsRepository(lastBackupAt = null, reminderThresholdDays = 7)
        val viewModel: BackupViewModel = viewModelFor(sqliteContent(schemaVersion = 1), neverBackedUpSettings)

        assertThat(viewModel.state.value.isBackupOverdue).isFalse()
    }

    private fun viewModelFor(
        content: ByteArray,
        settings: FakeBackupSettingsRepository = this.settings,
    ): BackupViewModel {
        val documents = FakeBackupDocuments(name = "gema-20260910-1432.gema", content = content)
        val validate = ValidateBackupUseCase(supportedSchemaVersion = 1)
        return BackupViewModel(
            observeBackupStatus = ObserveBackupStatusUseCase(settings, clock),
            createBackup = CreateBackupUseCase(store, settings, clock),
            inspectBackup = InspectBackupUseCase(documents, validate),
            restoreBackup = RestoreBackupUseCase(documents, store, validate),
            setReminderThreshold = SetReminderThresholdUseCase(settings),
            getSchoolYears = GetSchoolYearsUseCase(schoolYears),
            countAllStudents = CountAllStudentsUseCase(schoolYears, sections, students),
            clock = clock,
        )
    }

    private fun aStudent(id: String, code: String, withdrawalDate: LocalDate? = null): Student = Student(
        id = StudentId(id),
        sectionId = SectionId("section-1"),
        code = StudentCode(code),
        fullName = "ACOSTA RIVERA, Luz Maria",
        withdrawalDate = withdrawalDate,
    )
}
