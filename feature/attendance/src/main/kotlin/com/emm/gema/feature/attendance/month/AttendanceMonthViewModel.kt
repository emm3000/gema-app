package com.emm.gema.feature.attendance.month

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.attendance.ExportMonthlyAttendanceUseCase
import com.emm.gema.core.domain.attendance.GetMonthlyAttendanceSummaryUseCase
import com.emm.gema.core.domain.attendance.MonthlyAttendanceSummary
import com.emm.gema.core.domain.attendance.StudentAttendanceMonthCount
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.siagie.AttendanceExportFile
import com.emm.gema.feature.attendance.asMonthLabel
import com.emm.gema.feature.attendance.title
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.YearMonth

private const val EXPORT_FAILED_MESSAGE: String = "No se pudo generar el archivo de asistencia"
private const val XLSX_MIME_TYPE: String = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

private val xlsxMimeTypes: List<String> = listOf(XLSX_MIME_TYPE, "application/vnd.ms-excel")

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceMonthViewModel(
    private val sectionId: String,
    initialMonth: YearMonth?,
    private val getSection: GetSectionUseCase,
    private val getMonthlySummary: GetMonthlyAttendanceSummaryUseCase,
    private val exportMonthlyAttendance: ExportMonthlyAttendanceUseCase,
    clock: Clock,
) : ViewModel() {

    private val month: MutableStateFlow<YearMonth> = MutableStateFlow(initialMonth ?: YearMonth.now(clock))

    private val _state: MutableStateFlow<AttendanceMonthUiState> = MutableStateFlow(AttendanceMonthUiState())
    val state: StateFlow<AttendanceMonthUiState> = _state.asStateFlow()

    private val _effects: Channel<AttendanceMonthUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<AttendanceMonthUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val section: Section? = getSection(sectionId)
            _state.value = _state.value.copy(sectionTitle = section?.title().orEmpty())
        }
        viewModelScope.launch {
            month.flatMapLatest { selected: YearMonth -> getMonthlySummary(sectionId, selected) }
                .collect { summary: MonthlyAttendanceSummary -> _state.value = summaryOf(summary) }
        }
    }

    fun onIntent(intent: AttendanceMonthUiIntent) {
        when (intent) {
            AttendanceMonthUiIntent.PreviousMonthClicked -> month.value = month.value.minusMonths(1)
            AttendanceMonthUiIntent.NextMonthClicked -> month.value = month.value.plusMonths(1)
            is AttendanceMonthUiIntent.MonthPicked -> month.value = intent.value
            AttendanceMonthUiIntent.ExportClicked -> emit(AttendanceMonthUiEffect.OpenDocumentPicker(xlsxMimeTypes))
            is AttendanceMonthUiIntent.TemplatePicked -> export(intent.uri)
            AttendanceMonthUiIntent.BackClicked -> emit(AttendanceMonthUiEffect.NavigateBack)
        }
    }

    private fun export(templateUri: String) {
        val selected: YearMonth = month.value
        _state.value = _state.value.copy(isExporting = true)
        viewModelScope.launch {
            runCatching { exportMonthlyAttendance(sectionId, selected, templateUri) }
                .onSuccess { file: AttendanceExportFile ->
                    emit(AttendanceMonthUiEffect.ShareFile(file.path, XLSX_MIME_TYPE))
                }
                .onFailure { emit(AttendanceMonthUiEffect.ShowMessage(EXPORT_FAILED_MESSAGE)) }
            _state.value = _state.value.copy(isExporting = false)
        }
    }

    private fun summaryOf(summary: MonthlyAttendanceSummary): AttendanceMonthUiState {
        val selected: YearMonth = month.value
        return _state.value.copy(
            isLoading = false,
            month = selected,
            monthLabel = selected.asMonthLabel(),
            recordedDayCount = summary.recordedDayCount,
            rows = summary.rows.map { it.toRow() },
            canExport = summary.recordedDayCount > 0,
        )
    }

    private fun emit(effect: AttendanceMonthUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun StudentAttendanceMonthCount.toRow(): AttendanceMonthRow = AttendanceMonthRow(
        studentId = studentId,
        displayName = displayName,
        countsByStatus = countsByStatus,
    )
}
