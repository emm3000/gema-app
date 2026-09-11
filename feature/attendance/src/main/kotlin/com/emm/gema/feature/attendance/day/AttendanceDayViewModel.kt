package com.emm.gema.feature.attendance.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.attendance.AttendanceEntry
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.attendance.GetAttendanceDayUseCase
import com.emm.gema.core.domain.attendance.RecordAttendanceUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.feature.attendance.asDayLabel
import com.emm.gema.feature.attendance.title
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceDayViewModel(
    private val sectionId: SectionId,
    initialDate: LocalDate?,
    private val getSection: GetSectionUseCase,
    private val getAttendanceDay: GetAttendanceDayUseCase,
    private val recordAttendance: RecordAttendanceUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(clock)
    private val date: MutableStateFlow<LocalDate> = MutableStateFlow(initialDate ?: today)

    private val _state: MutableStateFlow<AttendanceDayUiState> = MutableStateFlow(AttendanceDayUiState())
    val state: StateFlow<AttendanceDayUiState> = _state.asStateFlow()

    private val _effects: Channel<AttendanceDayUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<AttendanceDayUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val section: Section? = getSection(sectionId)
            _state.value = _state.value.copy(sectionTitle = section?.title().orEmpty())
        }
        viewModelScope.launch {
            date.flatMapLatest { selected: LocalDate -> getAttendanceDay(sectionId, selected) }
                .collect { entries: List<AttendanceEntry> -> _state.value = dayOf(entries) }
        }
    }

    fun onIntent(intent: AttendanceDayUiIntent) {
        when (intent) {
            is AttendanceDayUiIntent.StatusSelected -> record(listOf(intent.studentId to intent.status))
            is AttendanceDayUiIntent.DatePicked -> moveTo(intent.value)
            AttendanceDayUiIntent.PreviousDayClicked -> moveTo(date.value.minusDays(1))
            AttendanceDayUiIntent.NextDayClicked -> moveTo(date.value.plusDays(1))
            AttendanceDayUiIntent.MarkAllPresent -> markAllPresent()
            AttendanceDayUiIntent.MonthlySummaryClicked -> emit(
                AttendanceDayUiEffect.NavigateToAttendanceMonth(sectionId, YearMonth.from(date.value)),
            )
            AttendanceDayUiIntent.BackClicked -> emit(AttendanceDayUiEffect.NavigateBack)
        }
    }

    private fun markAllPresent() {
        val pending: List<Pair<StudentId, AttendanceStatus>> = _state.value.rows
            .filterNot { it.isRecorded }
            .map { it.studentId to AttendanceStatus.PRESENT }

        record(pending)
    }

    private fun record(entries: List<Pair<StudentId, AttendanceStatus>>) {
        if (entries.isEmpty()) return
        val selected: LocalDate = date.value

        viewModelScope.launch {
            runCatching {
                entries.forEach { (studentId: StudentId, status: AttendanceStatus) ->
                    recordAttendance(sectionId, studentId, selected, status)
                }
            }.onFailure { _effects.send(AttendanceDayUiEffect.ShowMessage(AttendanceDayMessage.RECORD_FAILED)) }
        }
    }

    private fun moveTo(value: LocalDate) {
        if (value.isAfter(today)) {
            emit(AttendanceDayUiEffect.ShowMessage(AttendanceDayMessage.FUTURE_DATE))
            return
        }
        date.value = value
    }

    private fun dayOf(entries: List<AttendanceEntry>): AttendanceDayUiState {
        val selected: LocalDate = date.value
        val rows: List<AttendanceRow> = entries.map { it.toRow() }

        return _state.value.copy(
            isLoading = false,
            date = selected,
            dateLabel = selected.asDayLabel(),
            canGoForward = selected.isBefore(today),
            presentCount = rows.count { it.status == AttendanceStatus.PRESENT },
            totalCount = rows.size,
            unmarkedCount = rows.count { !it.isRecorded },
            rows = rows,
        )
    }

    private fun emit(effect: AttendanceDayUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun AttendanceEntry.toRow(): AttendanceRow = AttendanceRow(
        studentId = student.id,
        displayName = student.fullName,
        status = status,
        isRecorded = isRecorded,
    )
}
