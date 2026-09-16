package com.emm.gema.feature.sections.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.domain.attendance.AttendanceEntry
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.attendance.summarise
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.GetSectionDetailExtrasUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.title
import com.emm.gema.core.domain.student.GetStudentsUseCase
import com.emm.gema.core.domain.student.Student
import com.emm.gema.core.theme.DateNameProvider
import com.emm.gema.core.theme.todayLabelOf
import com.emm.gema.feature.sections.attendanceSummaryLabel
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SectionDetailViewModel(
    private val sectionId: SectionId,
    private val getSection: GetSectionUseCase,
    private val getStudents: GetStudentsUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getCurrentPeriod: GetCurrentPeriodUseCase,
    private val getSectionDetailExtras: GetSectionDetailExtrasUseCase,
    private val getAttendanceDay: GetAttendanceDayUseCase,
    private val clock: Clock,
    private val dateNames: DateNameProvider,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(clock)

    private val _state: MutableStateFlow<SectionDetailUiState> = MutableStateFlow(
        SectionDetailUiState(
            today = today,
            todayLabel = todayLabelOf(today, dateNames),
            todayAttendanceSummary = AttendanceDaySummary(0, 0, 0).attendanceSummaryLabel(),
        ),
    )
    val state: StateFlow<SectionDetailUiState> = _state.asStateFlow()

    private val _effects: Channel<SectionDetailUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<SectionDetailUiEffect> = _effects.receiveAsFlow()

    private var section: Section? = null

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch {
            getStudents(sectionId).collect { students: List<Student> ->
                _state.value = _state.value.copy(studentCount = students.count { !it.isWithdrawn })
            }
        }
        viewModelScope.launch {
            getAttendanceDay(sectionId, today).collect { entries: List<AttendanceEntry> ->
                val summary: AttendanceDaySummary = entries.summarise()
                _state.value = _state.value.copy(
                    todayAttendanceSummary = summary.attendanceSummaryLabel(),
                    isTodayAttendanceTaken = summary.isTaken,
                )
            }
        }
    }

    fun onIntent(intent: SectionDetailUiIntent) {
        when (intent) {
            SectionDetailUiIntent.TakeAttendanceClicked,
            SectionDetailUiIntent.AttendanceClicked,
            -> emit(SectionDetailUiEffect.NavigateToAttendanceDay(sectionId, today))
            SectionDetailUiIntent.StudentsClicked -> emit(SectionDetailUiEffect.NavigateToStudents(sectionId))
            SectionDetailUiIntent.PeriodLevelsClicked -> emit(SectionDetailUiEffect.NavigateToPeriodLevels(sectionId))
            SectionDetailUiIntent.ExportClicked -> emit(SectionDetailUiEffect.NavigateToExport(sectionId))
            SectionDetailUiIntent.ActivitiesClicked -> emit(SectionDetailUiEffect.NavigateToActivities(sectionId))
            SectionDetailUiIntent.AreasClicked -> emit(SectionDetailUiEffect.NavigateToSectionAreas(sectionId))
            SectionDetailUiIntent.RenameClicked -> rename()
            SectionDetailUiIntent.BackClicked -> emit(SectionDetailUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val stored: Section? = getSection(sectionId)
        section = stored
        _state.value = _state.value.copy(isLoading = false, sectionTitle = stored?.title().orEmpty())

        val schoolYearId: SchoolYearId = stored?.schoolYearId ?: return
        val schoolYear: SchoolYear = getSchoolYear(schoolYearId) ?: return
        val currentPeriod: Period = getCurrentPeriod(schoolYearId) ?: return

        _state.value = _state.value.copy(
            currentPeriodLabel = schoolYear.periodKind.labelFor(currentPeriod.number),
            hasStoredTemplate = getSectionDetailExtras.hasStoredTemplate(sectionId),
        )

        viewModelScope.launch {
            getSectionDetailExtras.activityCount(sectionId, currentPeriod.id).collect { count: Int ->
                _state.value = _state.value.copy(activityCount = count)
            }
        }

        getSectionDetailExtras.missingPeriodLevelCount(sectionId, currentPeriod.id).collect { missing: Int ->
            _state.value = _state.value.copy(missingPeriodLevelCount = missing)
        }
    }

    private fun rename() {
        val schoolYearId: SchoolYearId = section?.schoolYearId ?: return
        emit(SectionDetailUiEffect.NavigateToSectionForm(schoolYearId, sectionId))
    }

    private fun emit(effect: SectionDetailUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
