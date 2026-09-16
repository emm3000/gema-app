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
import com.emm.gema.core.domain.date.DateNameProvider
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.curriculum.WorkedCompetencyRepository
import com.emm.gema.core.domain.evaluation.GetMissingPeriodLevelCountUseCase
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelRepository
import com.emm.gema.core.domain.schoolyear.ActiveSchoolYearRepository
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.PeriodRepository
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.schoolyear.SchoolYearRepository
import com.emm.gema.core.domain.schoolyear.divide
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionsUseCase
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionAreaRepository
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.SectionRepository
import com.emm.gema.core.domain.student.GetStudentCountsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.domain.student.StudentCode
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.domain.student.StudentRepository
import com.emm.gema.core.domain.student.orderedByName
import com.google.common.truth.Truth.assertThat
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val REMINDER_THRESHOLD_DAYS: Int = 7

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now: Instant = Instant.parse("2026-09-10T19:32:00Z")
    private val backupClock: Clock = Clock.fixed(now, ZoneId.of("America/Lima"))
    private val schoolYear = SchoolYear(
        id = SchoolYearId("2026"),
        label = "2026",
        startDate = LocalDate.of(2026, 3, 2),
        endDate = LocalDate.of(2026, 12, 18),
        periodKind = PeriodKind.BIMESTER,
    )
    private val periods: List<Period> = PeriodKind.BIMESTER
        .divide(schoolYear.startDate, schoolYear.endDate)
        .map { Period(PeriodId("period-${it.number}"), schoolYear.id, it.number, it.startDate, it.endDate) }
    private val schoolYearRepository = FakeSchoolYearRepository(listOf(schoolYear))
    private val periodRepository = FakePeriodRepository(periods)
    private val sectionRepository = FakeSectionRepository(
        listOf(
            Section(SectionId("section-2"), schoolYear.id, Grade.FOURTH, "B"),
            Section(SectionId("section-1"), schoolYear.id, Grade.THIRD, "A"),
        )
    )
    private val activeSchoolYearRepository = FakeActiveSchoolYearRepository(schoolYear.id)
    private val dateNames = FakeDateNameProvider()
    private val attendanceRepository = FakeAttendanceRepository()
    private val workedCompetencyRepository = FakeWorkedCompetencyRepository()
    private val studentRepository = FakeStudentRepository(
        listOf(
            student("student-1", SectionId("section-1"), "12345678901234"),
            student("student-2", SectionId("section-1"), "12345678901235"),
            student("student-3", SectionId("section-2"), "12345678901236"),
        )
    )

    @Test
    fun `the active school year, its current period and its sections are shown`() {
        val state: HomeUiState = homeAt(periods[1].startDate).state.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.schoolYearLabel).isEqualTo("2026")
        assertThat(state.currentPeriodLabel).isEqualTo("II Bimestre")
        assertThat(state.sections.map { it.title }).containsExactly("3ro A", "4to B").inOrder()
        assertThat(state.sections.map { it.studentCount }).containsExactly(2, 1).inOrder()
    }

    @Test
    fun `the current period shows the days left and its end date`() {
        val state: HomeUiState = homeAt(periods[1].startDate).state.value

        val expectedDaysLeft: Int = ChronoUnit.DAYS.between(periods[1].startDate, periods[1].endDate).toInt()
        assertThat(state.daysLeftInPeriod).isEqualTo(expectedDaysLeft)
        assertThat(state.currentPeriodEndDate).isEqualTo(periods[1].endDate)
    }

    @Test
    fun `there is no period countdown outside the school year`() {
        val state: HomeUiState = homeAt(LocalDate.of(2027, 1, 5)).state.value

        assertThat(state.daysLeftInPeriod).isNull()
        assertThat(state.currentPeriodEndDate).isNull()
    }

    @Test
    fun `there is no current period label outside the school year`() {
        assertThat(homeAt(LocalDate.of(2027, 1, 5)).state.value.currentPeriodLabel).isNull()
    }

    @Test
    fun `opening a section goes to its hub`() = runTest {
        val viewModel: HomeViewModel = homeAt(schoolYear.startDate)

        viewModel.effects.test {
            viewModel.onIntent(HomeUiIntent.SectionClicked(SectionId("section-1")))

            assertThat(awaitItem()).isEqualTo(HomeUiEffect.NavigateToSectionDetail(SectionId("section-1")))
        }
    }

    @Test
    fun `each section card says whether today's attendance is taken`() = runTest {
        assertThat(homeAt(schoolYear.startDate).state.value.sections.map { it.attendance.isTaken })
            .containsExactly(false, false)

        attendanceRepository.record(
            AttendanceRecord(
                SectionId("section-1"),
                StudentId("student-1"),
                schoolYear.startDate,
                AttendanceStatus.ABSENT,
            ),
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
            viewModel.onIntent(HomeUiIntent.TakeAttendanceClicked(SectionId("section-1")))

            assertThat(awaitItem())
                .isEqualTo(HomeUiEffect.NavigateToAttendanceDay(SectionId("section-1"), schoolYear.startDate))
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

    @Test
    fun `home leads with today's weekday, day and month`() {
        assertThat(homeAt(LocalDate.of(2026, 9, 10)).state.value.todayLabel)
            .isEqualTo("HOY · JUEVES 10 DE SETIEMBRE")
    }

    @Test
    fun `home leads with a Sunday`() {
        assertThat(homeAt(LocalDate.of(2026, 9, 13)).state.value.todayLabel)
            .isEqualTo("HOY · DOMINGO 13 DE SETIEMBRE")
    }

    @Test
    fun `home leads with the last day of December`() {
        assertThat(homeAt(LocalDate.of(2026, 12, 31)).state.value.todayLabel)
            .isEqualTo("HOY · JUEVES 31 DE DICIEMBRE")
    }

    @Test
    fun `each section counts the levels still missing in the current period`() {
        workedCompetencyRepository.worked.value = setOf(
            Triple(SectionId("section-1"), periods[1].id, Competency.idOf(Area.PPSS, 1)),
        )

        val sections: List<SectionRow> = homeAt(periods[1].startDate).state.value.sections

        assertThat(sections.map { it.missingLevelCount }).containsExactly(2, 0).inOrder()
    }

    @Test
    fun `there is no missing level count outside every period`() {
        val sections: List<SectionRow> = homeAt(LocalDate.of(2027, 1, 5)).state.value.sections

        assertThat(sections.map { it.missingLevelCount }).containsExactly(null, null)
    }

    private fun homeAt(today: LocalDate, lastBackupAt: Instant? = null): HomeViewModel =
        homeAtClock(Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC")), lastBackupAt)

    private fun homeAtClock(clock: Clock, lastBackupAt: Instant? = null): HomeViewModel {
        val settings = FakeBackupSettingsRepository(
            BackupSettings(lastBackupAt = lastBackupAt, reminderThresholdDays = REMINDER_THRESHOLD_DAYS),
        )
        return HomeViewModel(
            getActiveSchoolYear = GetActiveSchoolYearUseCase(activeSchoolYearRepository, schoolYearRepository),
            getSections = GetSectionsUseCase(sectionRepository),
            getCurrentPeriod = GetCurrentPeriodUseCase(repository = periodRepository, clock = clock),
            getStudentCounts = GetStudentCountsUseCase(studentRepository),
            observeBackupStatus = ObserveBackupStatusUseCase(settings, backupClock),
            getAttendanceDay = GetAttendanceDayUseCase(studentRepository, attendanceRepository),
            getMissingPeriodLevelCount = GetMissingPeriodLevelCountUseCase(
                sectionAreaRepository = FakeSectionAreaRepository(),
                workedCompetencyRepository = workedCompetencyRepository,
                studentRepository = studentRepository,
                periodLevelRepository = FakePeriodLevelRepository(),
            ),
            dateNames = dateNames,
            clock = clock,
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

        override suspend fun findById(id: SchoolYearId): SchoolYear? = schoolYears.value.find { it.id == id }

        override suspend fun save(schoolYear: SchoolYear) = Unit
    }

    private class FakePeriodRepository(private val stored: List<Period>) : PeriodRepository {

        override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Period>> =
            MutableStateFlow(stored.filter { it.schoolYearId == schoolYearId })

        override suspend fun findBySchoolYear(schoolYearId: SchoolYearId): List<Period> =
            stored.filter { it.schoolYearId == schoolYearId }

        override suspend fun findById(id: PeriodId): Period? = stored.find { it.id == id }

        override suspend fun saveAll(periods: List<Period>) = Unit
    }

    private class FakeSectionRepository(initial: List<Section>) : SectionRepository {

        private val sections: MutableStateFlow<List<Section>> = MutableStateFlow(initial)

        override fun observeBySchoolYear(schoolYearId: SchoolYearId): Flow<List<Section>> = sections
            .map { stored ->
                stored.filter { it.schoolYearId == schoolYearId }
                    .sortedWith(compareBy({ it.grade.number }, { it.name }))
            }

        override fun observeCountsBySchoolYear(): Flow<Map<SchoolYearId, Int>> = sections
            .map { stored -> stored.groupingBy { it.schoolYearId }.eachCount() }

        override suspend fun findById(id: SectionId): Section? = sections.value.find { it.id == id }

        override suspend fun save(section: Section) = Unit

        override suspend fun delete(id: SectionId) = Unit
    }

    private class FakeStudentRepository(initial: List<Student>) : StudentRepository {

        private val students: MutableStateFlow<List<Student>> = MutableStateFlow(initial)

        override fun observeBySection(sectionId: SectionId): Flow<List<Student>> = students
            .map { stored -> stored.filter { it.sectionId == sectionId }.orderedByName() }

        override fun observeCountsBySection(): Flow<Map<SectionId, Int>> = students
            .map { stored -> stored.filterNot { it.isWithdrawn }.groupingBy { it.sectionId }.eachCount() }

        override suspend fun listBySection(sectionId: SectionId): List<Student> = students.value
            .filter { it.sectionId == sectionId }
            .orderedByName()

        override suspend fun findById(id: StudentId): Student? = students.value.find { it.id == id }

        override suspend fun findByCode(sectionId: SectionId, code: StudentCode): Student? = students.value
            .find { it.sectionId == sectionId && it.code == code }

        override suspend fun save(student: Student) = Unit

        override suspend fun deleteBySection(sectionId: SectionId) = Unit
    }

    private class FakeAttendanceRepository : AttendanceRepository {

        private val records: MutableStateFlow<List<AttendanceRecord>> = MutableStateFlow(emptyList())

        override fun observeBySectionAndDate(sectionId: SectionId, date: LocalDate): Flow<List<AttendanceRecord>> =
            records.map { stored -> stored.filter { it.sectionId == sectionId && it.date == date } }

        override fun observeBySectionAndMonth(sectionId: SectionId, month: YearMonth): Flow<List<AttendanceRecord>> =
            records.map { stored ->
                stored.filter { it.sectionId == sectionId && YearMonth.from(it.date) == month }
            }

        override suspend fun record(record: AttendanceRecord) {
            records.value = records.value
                .filterNot { it.studentId == record.studentId && it.date == record.date } + record
        }

        override suspend fun countRecordedDays(sectionId: SectionId): Int = records.value
            .filter { it.sectionId == sectionId }
            .distinctBy { it.date }
            .size

        override suspend fun deleteBySection(sectionId: SectionId) {
            records.value = records.value.filterNot { it.sectionId == sectionId }
        }
    }

    private class FakeSectionAreaRepository : SectionAreaRepository {

        override fun observeHiddenAreas(sectionId: SectionId): Flow<Set<Area>> = MutableStateFlow(emptySet())

        override suspend fun setAreaHidden(sectionId: SectionId, area: Area, isHidden: Boolean) = Unit

        override suspend fun clearSection(sectionId: SectionId) = Unit
    }

    private class FakeWorkedCompetencyRepository : WorkedCompetencyRepository {

        val worked: MutableStateFlow<Set<Triple<SectionId, PeriodId, CompetencyId>>> = MutableStateFlow(emptySet())

        override fun observeWorked(sectionId: SectionId, periodId: PeriodId): Flow<Set<CompetencyId>> = worked
            .map { stored ->
                stored.filter { it.first == sectionId && it.second == periodId }.mapTo(mutableSetOf()) { it.third }
            }

        override suspend fun setWorked(
            sectionId: SectionId,
            periodId: PeriodId,
            competencyId: CompetencyId,
            isWorked: Boolean,
        ) = Unit

        override suspend fun clearSection(sectionId: SectionId) = Unit
    }

    private class FakePeriodLevelRepository : PeriodLevelRepository {

        override fun observeByPeriod(sectionId: SectionId, periodId: PeriodId): Flow<List<PeriodLevel>> =
            MutableStateFlow(emptyList())

        override fun observeRecordedCountsByPeriod(
            sectionId: SectionId,
            periodId: PeriodId,
        ): Flow<Map<CompetencyId, Int>> = MutableStateFlow(emptyMap())

        override fun observeRecordedCountsBySection(sectionId: SectionId): Flow<Map<CompetencyId, Int>> =
            MutableStateFlow(emptyMap())

        override suspend fun find(key: PeriodLevelKey): PeriodLevel? = null

        override suspend fun save(periodLevel: PeriodLevel) = Unit

        override suspend fun delete(key: PeriodLevelKey) = Unit

        override suspend fun clearSection(sectionId: SectionId) = Unit
    }

    private class FakeActiveSchoolYearRepository(initial: SchoolYearId?) : ActiveSchoolYearRepository {

        private val activeId: MutableStateFlow<SchoolYearId?> = MutableStateFlow(initial)

        override fun observeActiveId(): Flow<SchoolYearId?> = activeId

        override suspend fun activate(schoolYearId: SchoolYearId) {
            activeId.value = schoolYearId
        }
    }

    private class FakeDateNameProvider : DateNameProvider {
        private val weekdays: Map<DayOfWeek, String> = mapOf(
            DayOfWeek.MONDAY to "Lunes",
            DayOfWeek.TUESDAY to "Martes",
            DayOfWeek.WEDNESDAY to "Miércoles",
            DayOfWeek.THURSDAY to "Jueves",
            DayOfWeek.FRIDAY to "Viernes",
            DayOfWeek.SATURDAY to "Sábado",
            DayOfWeek.SUNDAY to "Domingo",
        )
        private val months: Map<Month, String> = mapOf(
            Month.JANUARY to "Enero",
            Month.FEBRUARY to "Febrero",
            Month.MARCH to "Marzo",
            Month.APRIL to "Abril",
            Month.MAY to "Mayo",
            Month.JUNE to "Junio",
            Month.JULY to "Julio",
            Month.AUGUST to "Agosto",
            Month.SEPTEMBER to "Setiembre",
            Month.OCTOBER to "Octubre",
            Month.NOVEMBER to "Noviembre",
            Month.DECEMBER to "Diciembre",
        )

        override fun weekdayName(dayOfWeek: DayOfWeek): String = weekdays.getValue(dayOfWeek)
        override fun monthName(month: Month): String = months.getValue(month)
        override fun todayPrefix(): String = "HOY"
    }
}

private fun student(id: String, sectionId: SectionId, code: String): Student = Student(
    id = StudentId(id),
    sectionId = sectionId,
    code = StudentCode(code),
    fullName = "ACOSTA RIVERA, Luz Maria",
)
