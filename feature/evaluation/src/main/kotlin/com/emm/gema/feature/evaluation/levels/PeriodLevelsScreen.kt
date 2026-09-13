package com.emm.gema.feature.evaluation.levels

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBadge
import com.emm.gema.core.ui.GBadgeTone
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.feature.evaluation.R
import com.emm.gema.core.ui.GDropdownPicker
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GLevelChip
import com.emm.gema.core.ui.GLevelChipSize
import com.emm.gema.core.ui.GPickerOption
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar

@Composable
fun PeriodLevelsScreen(
    state: PeriodLevelsUiState,
    onIntent: (PeriodLevelsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Niveles - ${state.sectionTitle}",
                subtitle = state.periodLabel,
                onBackClick = { onIntent(PeriodLevelsUiIntent.BackClicked) },
                actions = {
                    GButton(
                        text = "Competencias",
                        onClick = { onIntent(PeriodLevelsUiIntent.WorkedCompetenciesClicked) },
                        variant = GButtonVariant.TEXT,
                    )
                    GBadge(
                        text = stringResource(R.string.period_levels_missing_count, state.missingCount),
                        tone = if (state.isMissingFilterOn) GBadgeTone.PRIMARY else GBadgeTone.ERROR,
                        onClick = { onIntent(PeriodLevelsUiIntent.MissingFilterToggled) },
                    )
                },
            )
        },
        modifier = modifier,
        bottomAction = state.columnModeBar()?.let { bar ->
            {
                ColumnModeBar(
                    heading = bar.heading,
                    studentName = bar.studentName,
                    achievementLevel = bar.achievementLevel,
                    onIntent = onIntent,
                )
            }
        },
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (message != null) {
                GBanner(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    tone = GBannerTone.ERROR,
                    actionText = "Entendido",
                    onActionClick = onMessageDismissed,
                )
            }
            Selectors(state = state, onIntent = onIntent)
            if (state.hasWorkedCompetencies) {
                Grid(state = state, onIntent = onIntent)
            } else {
                GEmptyState(
                    title = "Todavía no elegiste competencias",
                    message = "Marca las competencias que trabajaste en esta área y este periodo.",
                    actionLabel = "Elegir competencias",
                    onActionClick = { onIntent(PeriodLevelsUiIntent.WorkedCompetenciesClicked) },
                )
            }
        }
    }

    if (state.sheet != null) {
        PeriodLevelSheet(sheet = state.sheet, onIntent = onIntent)
    }
}

@Composable
private fun Selectors(state: PeriodLevelsUiState, onIntent: (PeriodLevelsUiIntent) -> Unit) {
    val currentPeriodBadge: String = stringResource(R.string.period_levels_badge_current)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GemaSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GDropdownPicker(
            options = state.areas.map { GPickerOption(value = it.area, label = it.name) },
            selected = state.selectedArea,
            onSelect = { onIntent(PeriodLevelsUiIntent.AreaSelected(it)) },
            label = "Área",
            modifier = Modifier.weight(1f),
        )
        GDropdownPicker(
            options = state.periods.map {
                GPickerOption(
                    value = it.id,
                    label = it.label,
                    badge = currentPeriodBadge.takeIf { _ -> it.isCurrent },
                )
            },
            selected = state.selectedPeriodId,
            onSelect = { onIntent(PeriodLevelsUiIntent.PeriodSelected(it)) },
            label = "Periodo",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Grid(state: PeriodLevelsUiState, onIntent: (PeriodLevelsUiIntent) -> Unit) {
    val bandScroll: ScrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalDivider()
        GridHeader(columns = state.columns, bandScroll = bandScroll, onIntent = onIntent)
        HorizontalDivider()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.visibleRows, key = { it.studentId.value }) { row ->
                GridRow(row = row, bandScroll = bandScroll, currentCell = state.currentCell(), onIntent = onIntent)
                HorizontalDivider()
            }
            item { Legend() }
        }
    }
}

@Composable
private fun GridHeader(
    columns: List<CompetencyColumn>,
    bandScroll: ScrollState,
    onIntent: (PeriodLevelsUiIntent) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GemaSpacing.minimumTouchTarget),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GText(
                text = stringResource(R.string.period_levels_column_student),
                modifier = Modifier
                    .width(GemaSpacing.gridNameColumnWidth)
                    .padding(horizontal = GemaSpacing.medium),
                style = GTextStyle.LABEL_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.horizontalScroll(bandScroll),
                horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
            ) {
                columns.forEach { column ->
                    GButton(
                        text = column.siagieOrdinal.toSiagieOrdinal(),
                        onClick = { onIntent(PeriodLevelsUiIntent.EnterColumnMode(column.id)) },
                        modifier = Modifier.width(GemaSpacing.gridCellWidth),
                        variant = GButtonVariant.TEXT,
                    )
                }
            }
        }
    }
}

@Composable
private fun GridRow(
    row: PeriodLevelRow,
    bandScroll: ScrollState,
    currentCell: PeriodLevelCellKey?,
    onIntent: (PeriodLevelsUiIntent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(GemaSpacing.gridRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GText(
            text = row.displayName,
            modifier = Modifier
                .width(GemaSpacing.gridNameColumnWidth)
                .padding(horizontal = GemaSpacing.medium),
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.horizontalScroll(bandScroll),
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            row.cells.forEach { cell ->
                GLevelChip(
                    letter = cell.achievementLevel?.name,
                    hasUnworkedComment = cell.unworkedComment != null,
                    isIncomplete = cell.isIncomplete,
                    isCurrent = cell.competencyId == currentCell?.competencyId &&
                        row.studentId == currentCell.studentId,
                    size = GLevelChipSize.GRID,
                    onClick = {
                        onIntent(
                            PeriodLevelsUiIntent.CellClicked(
                                PeriodLevelCellKey(row.studentId, cell.competencyId),
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    GText(
        text = "C! falta la conclusión descriptiva. * comentario. Vacío es sin nivel.",
        modifier = Modifier.padding(vertical = GemaSpacing.screenGutter),
        style = GTextStyle.BODY_LARGE,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun Int.toSiagieOrdinal(): String = toString().padStart(2, '0')

private data class ColumnModeBarState(
    val heading: String,
    val studentName: String,
    val achievementLevel: AchievementLevel?,
)

private fun PeriodLevelsUiState.currentCell(): PeriodLevelCellKey? {
    val competencyId: CompetencyId = columnMode?.competencyId ?: return null
    val studentId: StudentId = columnModeStudent?.studentId ?: return null

    return PeriodLevelCellKey(studentId = studentId, competencyId = competencyId)
}

private fun PeriodLevelsUiState.columnModeBar(): ColumnModeBarState? {
    val mode: ColumnModeUiState = columnMode ?: return null
    val column: CompetencyColumn = columnModeColumn ?: return null
    val student: PeriodLevelRow = columnModeStudent ?: return null
    val position: Int = mode.currentStudentIndex + 1

    return ColumnModeBarState(
        heading = "${column.siagieOrdinal.toSiagieOrdinal()} ${column.name} - $position de ${visibleRows.size}",
        studentName = student.displayName,
        achievementLevel = student.cells.find { it.competencyId == mode.competencyId }
            ?.achievementLevel,
    )
}

@PreviewLightDark
@Composable
private fun PeriodLevelsScreenPreview() {
    GemaTheme {
        PeriodLevelsScreen(
            state = PeriodLevelsUiState(
                isLoading = false,
                sectionTitle = "3ro A",
                areas = listOf(AreaOption(Area.PPSS, "Personal Social")),
                selectedArea = Area.PPSS,
                periods = listOf(PeriodOption(PeriodId("period-2"), "II Bimestre", isCurrent = true)),
                selectedPeriodId = PeriodId("period-2"),
                columns = listOf(
                    CompetencyColumn(CompetencyId("PPSS-1"), 1, "Construye su identidad"),
                    CompetencyColumn(CompetencyId("PPSS-2"), 2, "Convive y participa democráticamente"),
                ),
                rows = listOf(
                    PeriodLevelRow(
                        studentId = StudentId("student-1"),
                        displayName = "ACOSTA RIVERA, Luz",
                        cells = listOf(
                            PeriodLevelCell(CompetencyId("PPSS-1"), AchievementLevel.AD, null, false, false),
                            PeriodLevelCell(CompetencyId("PPSS-2"), AchievementLevel.C, null, false, true),
                        ),
                    ),
                ),
                missingCount = 3,
                hasWorkedCompetencies = true,
            ),
            onIntent = {},
        )
    }
}
