package com.emm.gema.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.attendance.summarise
import com.emm.gema.core.domain.backup.BackupStatus
import com.emm.gema.core.domain.backup.ObserveBackupStatusUseCase
import com.emm.gema.core.domain.schoolyear.GetActiveSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.GetSectionsUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.title
import com.emm.gema.core.domain.student.GetStudentCountsUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    getActiveSchoolYear: GetActiveSchoolYearUseCase,
    private val getSections: GetSectionsUseCase,
    private val getCurrentPeriod: GetCurrentPeriodUseCase,
    private val getStudentCounts: GetStudentCountsUseCase,
    private val observeBackupStatus: ObserveBackupStatusUseCase,
    private val getAttendanceDay: GetAttendanceDayUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(clock)

    private val _state: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effects: Channel<HomeUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            getActiveSchoolYear()
                .flatMapLatest { schoolYear -> schoolYearState(schoolYear) }
                .collect { schoolYearState -> _state.value = merge(schoolYearState) }
        }
        viewModelScope.launch {
            observeBackupStatus().collect(::onBackupStatus)
        }
    }

    fun onIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.SectionClicked -> emit(HomeUiEffect.NavigateToSectionDetail(intent.id))
            is HomeUiIntent.TakeAttendanceClicked ->
                emit(HomeUiEffect.NavigateToAttendanceDay(intent.id, today))
            HomeUiIntent.AddSectionClicked -> withSchoolYear { HomeUiEffect.NavigateToSectionForm(it, null) }
            HomeUiIntent.SchoolYearSwitcherClicked -> emit(HomeUiEffect.NavigateToSchoolYears)
            HomeUiIntent.OutOfPeriodClicked -> withSchoolYear { HomeUiEffect.NavigateToPeriods(it) }
            HomeUiIntent.BackupReminderClicked -> emit(HomeUiEffect.NavigateToBackup)
        }
    }

    private suspend fun schoolYearState(schoolYear: SchoolYear?): Flow<HomeUiState> {
        if (schoolYear == null) return flowOf(HomeUiState(isLoading = false))

        val currentPeriod: Period? = getCurrentPeriod(schoolYear.id)
        val currentPeriodLabel: String? = currentPeriod?.let { period -> schoolYear.periodKind.labelFor(period.number) }
        val daysLeftInPeriod: Int? = currentPeriod?.let { period ->
            ChronoUnit.DAYS.between(today, period.endDate).toInt()
        }

        return combine(
            getSections(schoolYear.id),
            getStudentCounts(),
            attendanceSummaries(schoolYear.id),
        ) { sections, studentCounts, summaries ->
            HomeUiState(
                isLoading = false,
                schoolYearId = schoolYear.id,
                schoolYearLabel = schoolYear.label,
                currentPeriodLabel = currentPeriodLabel,
                daysLeftInPeriod = daysLeftInPeriod,
                currentPeriodEndDate = currentPeriod?.endDate,
                sections = sections.map { section ->
                    section.toRow(
                        studentCount = studentCounts[section.id] ?: 0,
                        attendance = summaries[section.id] ?: AttendanceDaySummary(0, 0, 0),
                    )
                },
            )
        }
    }

    private fun attendanceSummaries(schoolYearId: SchoolYearId): Flow<Map<SectionId, AttendanceDaySummary>> =
        getSections(schoolYearId).flatMapLatest { sections: List<Section> ->
            if (sections.isEmpty()) return@flatMapLatest flowOf(emptyMap())

            combine(sections.map { section -> getAttendanceDay(section.id, today) }) { days ->
                sections.mapIndexed { index: Int, section: Section ->
                    section.id to days[index].summarise()
                }.toMap()
            }
        }

    private fun merge(schoolYearState: HomeUiState): HomeUiState =
        schoolYearState.copy(backupReminder = _state.value.backupReminder)

    private fun onBackupStatus(status: BackupStatus) {
        _state.value = _state.value.copy(backupReminder = reminderOf(status))
    }

    private fun reminderOf(status: BackupStatus): BackupReminder? {
        if (!status.isReminderDue) return null
        return BackupReminder(
            daysSinceLastBackup = status.daysSinceLastBackup ?: 0,
            hasEverBackedUp = status.lastBackupAt != null,
        )
    }

    private fun withSchoolYear(effect: (SchoolYearId) -> HomeUiEffect) {
        val schoolYearId: SchoolYearId = _state.value.schoolYearId ?: return
        emit(effect(schoolYearId))
    }

    private fun emit(effect: HomeUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun Section.toRow(studentCount: Int, attendance: AttendanceDaySummary): SectionRow = SectionRow(
        id = id,
        title = title(),
        studentCount = studentCount,
        attendance = attendance,
    )
}
