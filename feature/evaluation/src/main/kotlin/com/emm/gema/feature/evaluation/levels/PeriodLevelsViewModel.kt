package com.emm.gema.feature.evaluation.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.gema.core.domain.activity.EvidenceRecord
import com.emm.gema.core.domain.curriculum.Competency
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.GetPeriodLevelGridUseCase
import com.emm.gema.core.domain.evaluation.GetPeriodLevelSheetContextUseCase
import com.emm.gema.core.domain.evaluation.PeriodLevel
import com.emm.gema.core.domain.evaluation.PeriodLevelGrid
import com.emm.gema.core.domain.evaluation.PeriodLevelKey
import com.emm.gema.core.domain.evaluation.PeriodLevelSheetContext
import com.emm.gema.core.domain.evaluation.SavePeriodLevelUseCase
import com.emm.gema.core.domain.schoolyear.GetCurrentPeriodUseCase
import com.emm.gema.core.domain.schoolyear.GetPeriodsUseCase
import com.emm.gema.core.domain.schoolyear.GetSchoolYearUseCase
import com.emm.gema.core.domain.schoolyear.Period
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.schoolyear.SchoolYear
import com.emm.gema.core.domain.schoolyear.labelFor
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.section.GetSectionAreasUseCase
import com.emm.gema.core.domain.section.GetSectionUseCase
import com.emm.gema.core.domain.section.Section
import com.emm.gema.core.domain.section.SectionArea
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.title
import com.emm.gema.core.domain.student.StudentId
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

class PeriodLevelsViewModel(
    private val sectionId: SectionId,
    private val initialCell: PeriodLevelCellKey? = null,
    private val getSection: GetSectionUseCase,
    private val getSchoolYear: GetSchoolYearUseCase,
    private val getPeriods: GetPeriodsUseCase,
    private val getCurrentPeriod: GetCurrentPeriodUseCase,
    private val getSectionAreas: GetSectionAreasUseCase,
    private val getPeriodLevelGrid: GetPeriodLevelGridUseCase,
    private val getPeriodLevelSheetContext: GetPeriodLevelSheetContextUseCase,
    private val savePeriodLevel: SavePeriodLevelUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<PeriodLevelsUiState> = MutableStateFlow(PeriodLevelsUiState())
    val state: StateFlow<PeriodLevelsUiState> = _state.asStateFlow()

    private val _effects: Channel<PeriodLevelsUiEffect> = Channel(Channel.BUFFERED)
    val effects: Flow<PeriodLevelsUiEffect> = _effects.receiveAsFlow()

    private val selection: MutableStateFlow<GridSelection?> = MutableStateFlow(null)

    private var pendingCell: PeriodLevelCellKey? = initialCell

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch { observeGrid() }
    }

    fun onIntent(intent: PeriodLevelsUiIntent) {
        when (intent) {
            is PeriodLevelsUiIntent.AreaSelected -> select(area = intent.area)
            is PeriodLevelsUiIntent.PeriodSelected -> select(periodId = intent.periodId)
            is PeriodLevelsUiIntent.CellClicked -> openCell(intent.key)
            is PeriodLevelsUiIntent.EnterColumnMode ->
                _state.value = _state.value.enteringColumnMode(intent.competencyId)

            PeriodLevelsUiIntent.MissingFilterToggled -> toggleMissingFilter()
            PeriodLevelsUiIntent.WorkedCompetenciesClicked -> openWorkedCompetencies()
            is PeriodLevelsUiIntent.SheetAchievementLevelSelected -> editSheet { it.withAchievementLevel(intent.level) }
            is PeriodLevelsUiIntent.SheetUnworkedCommentSelected -> editSheet { it.withUnworkedComment(intent.comment) }
            is PeriodLevelsUiIntent.SheetDescriptiveConclusionChanged ->
                editSheet { it.withDescriptiveConclusion(intent.value) }

            PeriodLevelsUiIntent.SheetDismissed -> _state.value = _state.value.copy(sheet = null)
            is PeriodLevelsUiIntent.PickLevelForCurrent -> recordAndAdvance(intent.level)
            PeriodLevelsUiIntent.ExitColumnMode -> _state.value = _state.value.leavingColumnMode()
            PeriodLevelsUiIntent.BackClicked -> emit(PeriodLevelsUiEffect.NavigateBack)
        }
    }

    private suspend fun load() {
        val section: Section = getSection(sectionId) ?: return
        val schoolYear: SchoolYear = getSchoolYear(section.schoolYearId) ?: return
        val periods: List<Period> = getPeriods(section.schoolYearId).first()
        val currentPeriod: Period? = getCurrentPeriod(section.schoolYearId)
        val areas: List<SectionArea> = getSectionAreas(sectionId).first().filter { it.isActive }
        val selectedPeriodId: PeriodId? = (currentPeriod ?: periods.firstOrNull())?.id
        val selectedArea: Area? = initialArea(areas) ?: areas.firstOrNull()?.area

        _state.value = _state.value.copy(
            isLoading = false,
            sectionTitle = section.title(),
            areas = areas.map { AreaOption(area = it.area, name = it.area.officialName) },
            selectedArea = selectedArea,
            periods = periods.map {
                PeriodOption(
                    id = it.id,
                    label = schoolYear.periodKind.labelFor(it.number),
                    isCurrent = it.id == currentPeriod?.id,
                )
            },
            selectedPeriodId = selectedPeriodId,
        )

        if (selectedArea != null && selectedPeriodId != null) {
            selection.value = GridSelection(area = selectedArea, periodId = selectedPeriodId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeGrid() {
        selection
            .filterNotNull()
            .flatMapLatest { getPeriodLevelGrid(sectionId = sectionId, periodId = it.periodId, area = it.area) }
            .collect { grid: PeriodLevelGrid -> render(grid) }
    }

    private fun render(grid: PeriodLevelGrid) {
        val rows: List<PeriodLevelRow> = grid.rows.map { row ->
            PeriodLevelRow(
                studentId = row.student.id,
                displayName = row.student.fullName,
                cells = row.cells.map { cell ->
                    PeriodLevelCell(
                        competencyId = cell.key.competencyId,
                        achievementLevel = cell.achievementLevel,
                        unworkedComment = cell.unworkedComment,
                        hasDescriptiveConclusion = cell.descriptiveConclusion.isNotBlank(),
                        isIncomplete = cell.isIncomplete,
                    )
                },
            )
        }

        _state.value = _state.value.copy(
            columns = grid.columns.map { it.toColumn() },
            rows = rows,
            missingCount = grid.missingCount,
            hasWorkedCompetencies = grid.columns.isNotEmpty(),
        )
        openPendingCell()
    }

    private fun initialArea(areas: List<SectionArea>): Area? = initialCell
        ?.let { Competency.areaOf(it.competencyId) }
        ?.takeIf { area: Area -> areas.any { it.area == area } }

    private fun openPendingCell() {
        val cell: PeriodLevelCellKey = pendingCell ?: return
        val isVisible: Boolean = _state.value.rows.any { it.studentId == cell.studentId } &&
            _state.value.columns.any { it.id == cell.competencyId }
        if (!isVisible) return

        pendingCell = null
        openCell(cell)
    }

    private fun select(area: Area? = null, periodId: PeriodId? = null) {
        val current: PeriodLevelsUiState = _state.value
        val nextArea: Area = area ?: current.selectedArea ?: return
        val nextPeriodId: PeriodId = periodId ?: current.selectedPeriodId ?: return

        _state.value = current.copy(
            selectedArea = nextArea,
            selectedPeriodId = nextPeriodId,
            sheet = null,
            columnMode = null,
        )
        selection.value = GridSelection(area = nextArea, periodId = nextPeriodId)
    }

    private fun toggleMissingFilter() {
        _state.value = _state.value.copy(isMissingFilterOn = !_state.value.isMissingFilterOn)
    }

    private fun openWorkedCompetencies() {
        val area: Area = _state.value.selectedArea ?: return
        val periodId: PeriodId = _state.value.selectedPeriodId ?: return

        emit(PeriodLevelsUiEffect.NavigateToWorkedCompetencies(sectionId, periodId, area))
    }

    private fun openCell(cell: PeriodLevelCellKey) {
        val current: PeriodLevelsUiState = _state.value
        val row: PeriodLevelRow = current.rows.find { it.studentId == cell.studentId } ?: return
        val column: CompetencyColumn = current.columns.find { it.id == cell.competencyId } ?: return
        val key: PeriodLevelKey = keyOf(cell.studentId, cell.competencyId) ?: return

        viewModelScope.launch {
            val context: PeriodLevelSheetContext = getPeriodLevelSheetContext(key)
            val stored: PeriodLevel = context.periodLevel

            _state.value = _state.value.copy(
                columnMode = null,
                sheet = PeriodLevelSheetUiState(
                    studentId = cell.studentId,
                    competencyId = cell.competencyId,
                    studentName = row.displayName,
                    competencyLabel = column.label(),
                    achievementLevel = stored.achievementLevel,
                    unworkedComment = stored.unworkedComment,
                    descriptiveConclusion = stored.descriptiveConclusion,
                    evidence = context.evidence.map { it.toRow() },
                ),
            )
        }
    }

    private fun editSheet(change: (PeriodLevel) -> PeriodLevel) {
        val sheet: PeriodLevelSheetUiState = _state.value.sheet ?: return
        val key: PeriodLevelKey = keyOf(sheet.studentId, sheet.competencyId) ?: return

        viewModelScope.launch {
            val updated: PeriodLevel = change(getPeriodLevelSheetContext(key).periodLevel)

            _state.value = _state.value.copy(
                sheet = sheet.copy(
                    achievementLevel = updated.achievementLevel,
                    unworkedComment = updated.unworkedComment,
                    descriptiveConclusion = updated.descriptiveConclusion,
                ),
            )
            persist(updated)
        }
    }

    private fun recordAndAdvance(level: AchievementLevel?) {
        val current: PeriodLevelsUiState = _state.value
        val mode: ColumnModeUiState = current.columnMode ?: return
        val student: PeriodLevelRow = current.columnModeStudent ?: return
        val key: PeriodLevelKey = keyOf(student.studentId, mode.competencyId) ?: return

        viewModelScope.launch {
            val stored: PeriodLevel = getPeriodLevelSheetContext(key).periodLevel
            persist(stored.withAchievementLevel(level))
            _state.value = _state.value.advancedToNextStudent()
        }
    }

    private suspend fun persist(periodLevel: PeriodLevel) {
        runCatching { savePeriodLevel(periodLevel) }
            .onFailure { _effects.send(PeriodLevelsUiEffect.ShowMessage(PeriodLevelsMessage.SAVE_FAILED)) }
    }

    private fun keyOf(studentId: StudentId, competencyId: CompetencyId): PeriodLevelKey? {
        val periodId: PeriodId = _state.value.selectedPeriodId ?: return null

        return PeriodLevelKey(
            sectionId = sectionId,
            periodId = periodId,
            studentId = studentId,
            competencyId = competencyId,
        )
    }

    private fun emit(effect: PeriodLevelsUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

private data class GridSelection(
    val area: Area,
    val periodId: PeriodId,
)

private fun Competency.toColumn(): CompetencyColumn = CompetencyColumn(
    id = id,
    siagieOrdinal = siagieOrdinal,
    name = name,
)

private fun CompetencyColumn.label(): String = "${siagieOrdinal.toString().padStart(2, '0')} $name"

private fun EvidenceRecord.toRow(): EvidenceRow = EvidenceRow(
    activityId = activityId,
    activityName = activityName,
    date = date,
    achievementLevel = achievementLevel,
)
