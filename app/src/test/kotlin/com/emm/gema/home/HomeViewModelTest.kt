package com.emm.gema.home

import app.cash.turbine.test
import com.emm.gema.MainDispatcherRule
import com.emm.gema.core.domain.attendance.AttendanceRecord
import com.emm.gema.core.domain.attendance.AttendanceRepository
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.backup.BackupSettings
import com.emm.gema.core.domain.backup.BackupSettingsRepository
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.schoolyear.divide
import com.emm.gema.core.domain.section.GetSectionsUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.GetStudentCountsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val REMINDER_THRESHOLD_DAYS: Int = 7

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now: Instant = Instant.parse("2026-09-10T19:32:00Z")
    private val backupClock: Clock = Clock.fixed(now, ZoneId.of("America/Lima"))
    private val schoolYear = SchoolYear(
        id = "2026",
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val periods: List<Period> = PeriodKind.BIMESTER
        .divide(schoolYear.startDate, schoolYear.endDate)
        .map { Period("period-${it.number}", schoolYear.id, it.number, it.startDate, it.endDate) }
    private val schoolYearRepository = FakeSchoolYearRepository(listOf(schoolYear))
    private val periodRepository = FakePeriodRepository(periods)
    private val sectionRepository = FakeSectionRepository(
        listOf(
            Section("section-2", schoolYear.id, Grade.FOURTH, "B"),
            Section("section-1", schoolYear.id, Grade.THIRD, "A"),
        )
    )
    private val activeSchoolYearRepository = FakeActiveSchoolYearRepository(schoolYear.id)
    private val attendanceRepository = FakeAttendanceRepository()
    private val studentRepository = FakeStudentRepository(
        listOf(
            student("student-1", "section-1", "12345678901234"),
            student("student-2", "section-1", "12345678901235"),
            student("student-3", "section-2", "12345678901236"),
        )
    )

    @Test
    fun `the active school year, its current period and its sections are shown`() {
        val state: HomeUiState = homeAt(periods[1].startDate).state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.schoolYearLabel).isEqualTo("2026")
        assertThat(state.currentPeriodLabel).isEqualTo("II Bimestre")
        assertThat(state.sections.map { it.title }).containsExactly("3° A", "4° B").inOrder()
        assertThat(state.sections.map { it.studentCount }).containsExactly(2, 1).inOrder()
    }

    @Test
    fun `there is no current period label outside the school year`() {
        assertThat(homeAt(LocalDate.of(2027, 1, 5)).state.value.currentPeriodLabel).isNull()
    }

    @Test
    fun `opening a section goes to its hub`() = runTest {
        val viewModel: HomeViewModel = homeAt(schoolYear.startDate)

        viewModel.effects.test {
            viewModel.onIntent(HomeUiIntent.SectionClicked("section-1"))

            assertThat(awaitItem()).isEqualTo(HomeUiEffect.NavigateToSectionDetail("section-1"))
        }
    }

    @Test
    fun `each section card says whether today's attendance is taken`() = runTest {
        assertThat(homeAt(schoolYear.startDate).state.value.sections.map { it.attendance.isTaken })
            .containsExactly(false, false)

        attendanceRepository.record(
            AttendanceRecord("section-1", "student-1", schoolYear.startDate, AttendanceStatus.ABSENT),
        )

        val sections: List<SectionRow> = homeAt(schoolYear.startDate).state.value.sections
        assertThat(sections.map { it.attendance.isTaken }).containsExactly(true, false)
        assertThat(sections.first().attendance.presentCount).isEqualTo(1)
        assertThat(sections.first().attendance.totalCount).isEqualTo(2)
    }

    @Test
    fun `the section card takes attendance for today without opening the hub`() = runTest {
        val viewModel: HomeViewModel = homeAt(schoolYear.startDate)

        viewModel.effects.test {
            viewModel.onIntent(HomeUiIntent.TakeAttendanceClicked("section-1"))

            assertThat(awaitItem())
                .isEqualTo(HomeUiEffect.NavigateToAttendanceDay("section-1", schoolYear.startDate))
        }
    }

    @Test
    fun `adding a section opens an empty form for the active year`() = runTest {
        val viewModel: HomeViewModel = homeAt(schoolYear.startDate)

        viewModel.effects.test {
            viewModel.onIntent(HomeUiIntent.AddSectionClicked)

            assertThat(awaitItem()).isEqualTo(HomeUiEffect.NavigateToSectionForm(schoolYear.id, null))
        }
    }

    @Test
    fun `the year switcher opens the school years screen`() = runTest {
        val viewModel: HomeViewModel = homeAt(schoolYear.startDate)

        viewModel.effects.test {
            viewModel.onIntent(HomeUiIntent.SchoolYearSwitcherClicked)

            assertThat(awaitItem()).isEqualTo(HomeUiEffect.NavigateToSchoolYears)
        }
    }

    @Test
    fun `a teacher who never backed up is reminded on home`() {
        val reminder: BackupReminder? = homeAt(schoolYear.startDate, lastBackupAt = null)
            .state.value.backupReminder

        assertThat(reminder).isNotNull()
        assertThat(reminder?.hasEverBackedUp).isFalse()
    }

    @Test
    fun `a backup older than the threshold is reminded on home`() {
        val reminder: BackupReminder? = homeAt(schoolYear.startDate, lastBackupAt = daysAgo(12))
            .state.value.backupReminder

        assertThat(reminder?.daysSinceLastBackup).isEqualTo(12)
        assertThat(reminder?.hasEverBackedUp).isTrue()
    }

    @Test
    fun `a recent backup shows no reminder on home`() {
        assertThat(homeAt(schoolYear.startDate, lastBackupAt = daysAgo(2)).state.value.backupReminder)
            .isNull()
    }

    @Test
    fun `tapping the reminder opens the backup screen`() = runTest {
        val viewModel: HomeViewModel = homeAt(schoolYear.startDate)

        viewModel.effects.test {
            viewModel.onIntent(HomeUiIntent.BackupReminderClicked)

            assertThat(awaitItem()).isEqualTo(HomeUiEffect.NavigateToBackup)
        }
    }

    private fun homeAt(today: LocalDate, lastBackupAt: Instant? = null): HomeViewModel {
        val settings = FakeBackupSettingsRepository(
            BackupSettings(lastBackupAt = lastBackupAt, reminderThresholdDays = REMINDER_THRESHOLD_DAYS),
        )
        return HomeViewModel(
            getActiveSchoolYear = GetActiveSchoolYearUseCase(activeSchoolYearRepository, schoolYearRepository),
            getSections = GetSectionsUseCase(sectionRepository),
            getCurrentPeriod = GetCurrentPeriodUseCase(
                repository = periodRepository,
                clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC")),
            ),
            getStudentCounts = GetStudentCountsUseCase(studentRepository),
            observeBackupStatus = ObserveBackupStatusUseCase(settings, backupClock),
            getAttendanceDay = GetAttendanceDayUseCase(studentRepository, attendanceRepository),
            clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC")),
        )
    }

    private fun daysAgo(days: Long): Instant = now.minus(days, ChronoUnit.DAYS)

    private class FakeBackupSettingsRepository(
        private val settings: BackupSettings,
    ) : BackupSettingsRepository {

        override fun observeSettings(): Flow<BackupSettings> = MutableStateFlow(settings)

        override suspend fun setLastBackupAt(instant: Instant) = Unit

        override suspend fun setReminderThresholdDays(days: Int) = Unit
    }

    private class FakeSchoolYearRepository(initial: List<SchoolYear>) : SchoolYearRepository {

        private val schoolYears: MutableStateFlow<List<SchoolYear>> = MutableStateFlow(initial)

        override fun observeAll(): Flow<List<SchoolYear>> = schoolYears

        override suspend fun findById(id: String): SchoolYear? = schoolYears.value.find { it.id == id }

        override suspend fun save(schoolYear: SchoolYear) = Unit
    }

    private class FakePeriodRepository(private val stored: List<Period>) : PeriodRepository {

        override fun observeBySchoolYear(schoolYearId: String): Flow<List<Period>> =
            MutableStateFlow(stored.filter { it.schoolYearId == schoolYearId })

        override suspend fun findBySchoolYear(schoolYearId: String): List<Period> =
            stored.filter { it.schoolYearId == schoolYearId }

        override suspend fun findById(id: String): Period? = stored.find { it.id == id }

        override suspend fun saveAll(periods: List<Period>) = Unit
    }

    private class FakeSectionRepository(initial: List<Section>) : SectionRepository {

        private val sections: MutableStateFlow<List<Section>> = MutableStateFlow(initial)

        override fun observeBySchoolYear(schoolYearId: String): Flow<List<Section>> = sections
            .map { stored ->
                stored.filter { it.schoolYearId == schoolYearId }
                    .sortedWith(compareBy({ it.grade.number }, { it.name }))
            }

        override fun observeCountsBySchoolYear(): Flow<Map<String, Int>> = sections
            .map { stored -> stored.groupingBy { it.schoolYearId }.eachCount() }

        override suspend fun findById(id: String): Section? = sections.value.find { it.id == id }

        override suspend fun save(section: Section) = Unit

        override suspend fun delete(id: String) = Unit
    }

    private class FakeStudentRepository(initial: List<Student>) : StudentRepository {

        private val students: MutableStateFlow<List<Student>> = MutableStateFlow(initial)

        override fun observeBySection(sectionId: String): Flow<List<Student>> = students
            .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

        override fun observeCountsBySection(): Flow<Map<String, Int>> = students
            .map { stored -> stored.filterNot { it.isWithdrawn }.groupingBy { it.sectionId }.eachCount() }

        override suspend fun listBySection(sectionId: String): List<Student> = students.value
            .filter { it.sectionId == sectionId }
            .orderedByName()

        override suspend fun findById(id: String): Student? = students.value.find { it.id == id }

        override suspend fun findByCode(sectionId: String, code: StudentCode): Student? = students.value
            .find { it.sectionId == sectionId && it.code == code }

        override suspend fun save(student: Student) = Unit

        override suspend fun deleteBySection(sectionId: String) = Unit
    }

    private class FakeAttendanceRepository : AttendanceRepository {

        private val records: MutableStateFlow<List<AttendanceRecord>> = MutableStateFlow(emptyList())

        override fun observeBySectionAndDate(sectionId: String, date: LocalDate): Flow<List<AttendanceRecord>> =
            records.map { stored -> stored.filter { it.sectionId == sectionId && it.date == date } }

        override suspend fun record(record: AttendanceRecord) {
            records.value = records.value
                .filterNot { it.studentId == record.studentId && it.date == record.date } + record
        }

        override suspend fun countRecordedDays(sectionId: String): Int = records.value
            .filter { it.sectionId == sectionId }
            .distinctBy { it.date }
            .size

        override suspend fun deleteBySection(sectionId: String) {
            records.value = records.value.filterNot { it.sectionId == sectionId }
        }
    }

    private class FakeActiveSchoolYearRepository(initial: String?) : ActiveSchoolYearRepository {

        private val activeId: MutableStateFlow<String?> = MutableStateFlow(initial)

        override fun observeActiveId(): Flow<String?> = activeId

        override suspend fun activate(schoolYearId: String) {
            activeId.value = schoolYearId
        }
    }
}

private fun student(id: String, sectionId: String, code: String): Student = Student(
    id = id,
    sectionId = sectionId,
    code = StudentCode(code),
    fullName = "ACOSTA RIVERA, Luz Maria",
)
