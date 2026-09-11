package com.emm.gema.feature.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.export.ExportGap
import com.emm.gema.core.domain.export.ExportGradesUseCase
import com.emm.gema.core.domain.export.GetGradesExportPlanUseCase
import com.emm.gema.core.domain.export.GetGradesTemplateNameUseCase
import com.emm.gema.core.domain.export.GradesExportPlan
import com.emm.gema.core.domain.export.GradesExportResult
import com.emm.gema.core.domain.export.SIAGIE_GRADES_MIME_TYPE
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.siagie.SiagieCompetencyColumn
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.title
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val ORDINAL_DIGITS: Int = 2

class ExportViewModel(
    private val sectionId: String,
    private val getSection: GetSectionUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getPeriods: GetPeriodsUseCase,
    private val getCurrentPeriod: GetCurrentPeriodUseCase,
    private val getGradesTemplateName: GetGradesTemplateNameUseCase,
    private val getGradesExportPlan: GetGradesExportPlanUseCase,
    private val exportGrades: ExportGradesUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<ExportUiState> = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    private val _effects: Channel<ExportUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<ExportUiEffect> = _effects.receiveAsFlow()

    private val selectedPeriod: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch { observePlan() }
    }

    fun onIntent(intent: ExportUiIntent) {
        when (intent) {
            is ExportUiIntent.PeriodSelected -> selectPeriod(intent.periodId)
            ExportUiIntent.ExportGradesClicked -> generateFile()
            is ExportUiIntent.GapRowClicked -> emit(
                ExportUiEffect.NavigateToPeriodLevelCell(
                    sectionId = sectionId,
                    studentId = intent.row.studentId,
                    competencyId = intent.row.competencyId,
                ),
            )
            ExportUiIntent.ImportTemplateClicked -> emit(ExportUiEffect.NavigateToStudents(sectionId))
            ExportUiIntent.BackClicked -> emit(ExportUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val section: Section = getSection(sectionId) ?: return
        val schoolYear: SchoolYear = getSchoolYear(section.schoolYearId) ?: return
        val periods: List<Period> = getPeriods(section.schoolYearId).first()
        val currentPeriod: Period? = getCurrentPeriod(section.schoolYearId)
        val periodId: String? = (currentPeriod ?: periods.firstOrNull())?.id

        _state.value = _state.value.copy(
            isLoading = false,
            sectionTitle = section.title(),
            templateFileName = getGradesTemplateName(sectionId),
            periods = periods.map {
                PeriodOption(
                    id = it.id,
                    label = schoolYear.periodKind.labelFor(it.number),
                    isCurrent = it.id == currentPeriod?.id,
                )
            },
            selectedPeriodId = periodId,
        )
        selectedPeriod.value = periodId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observePlan() {
        selectedPeriod
            .filterNotNull()
            .flatMapLatest { periodId: String -> getGradesExportPlan(sectionId, periodId) }
            .collect(::render)
    }

    private fun render(plan: GradesExportPlan) {
        _state.value = _state.value.copy(gradesExportState = gradesStateOf(plan))
    }

    private fun gradesStateOf(plan: GradesExportPlan): GradesExportUiState = when {
        _state.value.templateFileName == null -> GradesExportUiState.Unavailable
        plan.isReady -> GradesExportUiState.Ready
        else -> GradesExportUiState.Blocked(plan.gaps.map { it.toRow() })
    }

    private fun selectPeriod(periodId: String) {
        _state.value = _state.value.copy(selectedPeriodId = periodId)
        selectedPeriod.value = periodId
    }

    private fun generateFile() {
        val periodId: String = _state.value.selectedPeriodId ?: return
        if (_state.value.isExporting) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true, templateMismatch = null)
            val result: Result<GradesExportResult> = runCatching { exportGrades(sectionId, periodId) }
            _state.value = _state.value.copy(isExporting = false)
            result
                .onSuccess(::onExported)
                .onFailure { emit(ExportUiEffect.ShowMessage(ExportMessage.EXPORT_FAILED)) }
        }
    }

    private fun onExported(result: GradesExportResult) {
        when (result) {
            GradesExportResult.Unavailable -> emit(ExportUiEffect.ShowMessage(ExportMessage.EXPORT_UNAVAILABLE))
            is GradesExportResult.Blocked ->
                _state.value = _state.value.copy(
                    gradesExportState = GradesExportUiState.Blocked(result.gaps.map { it.toRow() }),
                )

            is GradesExportResult.TemplateMismatch ->
                _state.value = _state.value.copy(
                    templateMismatch = TemplateMismatchUi(
                        areaNames = result.areas.map(Area::officialName),
                        studentNames = result.studentNames,
                        competencyLabels = result.competencies.map { column: SiagieCompetencyColumn ->
                            competencyLabelOf(column.area.officialName, column.siagieOrdinal)
                        },
                    ),
                )

            is GradesExportResult.Exported ->
                emit(ExportUiEffect.ShareFile(path = result.file.path, mimeType = SIAGIE_GRADES_MIME_TYPE))
        }
    }

    private fun emit(effect: ExportUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

private fun ExportGap.toRow(): ExportGapRow = ExportGapRow(
    studentId = studentId,
    studentName = studentName,
    competencyId = competency.id,
    competencyLabel = competencyLabelOf(competency.area.officialName, competency.siagieOrdinal),
)

private fun competencyLabelOf(areaName: String, siagieOrdinal: Int): String =
    "$areaName - ${siagieOrdinal.toString().padStart(ORDINAL_DIGITS, '0')}"
