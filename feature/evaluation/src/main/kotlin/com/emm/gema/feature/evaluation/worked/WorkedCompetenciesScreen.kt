package com.emm.gema.feature.evaluation.worked

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GCheckRow
import com.emm.gema.core.ui.GDivider
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.evaluation.R

@Composable
fun WorkedCompetenciesScreen(
    state: WorkedCompetenciesUiState,
    onIntent: (WorkedCompetenciesUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = "Competencias trabajadas",
                subtitle = "${state.areaName} · ${state.periodLabel}",
                onBackClick = { onIntent(WorkedCompetenciesUiIntent.BackClicked) },
                showHairline = listState.canScrollBackward,
            )
        },
        modifier = modifier,
        contentGutter = false,
        bottomAction = {
            WorkedCompetenciesFooter(
                selectedCount = state.selectedCount,
                totalCount = state.competencies.size,
                recordedLevelsWarning = state.competencies.recordedLevelsWarning(),
            )
        },
    ) { padding: PaddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = GemaSpacing.screenGutter),
        ) {
            if (message != null) {
                item {
                    GBanner(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.small),
                        tone = GBannerTone.ERROR,
                        actionText = stringResource(R.string.worked_competencies_understood),
                        onActionClick = onMessageDismissed,
                    )
                }
            }
            item {
                GText(
                    text = stringResource(R.string.worked_competencies_hint),
                    style = GTextStyle.BODY_LARGE,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.small),
                )
            }
            if (state.competencies.isNotEmpty()) {
                item {
                    GDivider()
                }
            }
            itemsIndexed(
                items = state.competencies,
                key = { _, row -> row.id.value },
            ) { index: Int, row: CompetencyToggleRow ->
                GCheckRow(
                    title = row.name,
                    isChecked = row.isWorked,
                    onCheckedChange = { onIntent(WorkedCompetenciesUiIntent.CompetencyToggled(row.id, it)) },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = row.siagieOrdinal.toSiagiePrefix(),
                    showBottomDivider = index != state.competencies.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun WorkedCompetenciesFooter(
    selectedCount: Int,
    totalCount: Int,
    recordedLevelsWarning: RecordedLevelsWarning?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        GDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = GemaSpacing.medium, bottom = GemaSpacing.small),
        ) {
            GText(
                text = pluralStringResource(
                    R.plurals.worked_competencies_marked_count,
                    selectedCount,
                    selectedCount,
                    totalCount,
                ),
                style = GTextStyle.LABEL_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (recordedLevelsWarning != null) {
                GBanner(
                    text = pluralStringResource(
                        R.plurals.worked_competencies_recorded_levels_warning,
                        recordedLevelsWarning.count,
                        recordedLevelsWarning.ordinals,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = GemaSpacing.small),
                    tone = GBannerTone.WARNING,
                    icon = Icons.Filled.Warning,
                )
            }
        }
    }
}

private fun Int.toSiagiePrefix(): String = toString().padStart(2, '0')

private data class RecordedLevelsWarning(val ordinals: String, val count: Int)

private fun List<CompetencyToggleRow>.recordedLevelsWarning(): RecordedLevelsWarning? {
    val withRecordedLevels: List<CompetencyToggleRow> = filter { it.recordedLevelCount > 0 }
    if (withRecordedLevels.isEmpty()) return null
    val ordinals: String = withRecordedLevels.joinToString(", ") { it.siagieOrdinal.toSiagiePrefix() }
    return RecordedLevelsWarning(ordinals = ordinals, count = withRecordedLevels.size)
}

@PreviewLightDark
@Composable
private fun WorkedCompetenciesScreenPreview() {
    GemaTheme {
        WorkedCompetenciesScreen(
            state = WorkedCompetenciesUiState(
                isLoading = false,
                areaName = "Personal Social",
                periodLabel = "II Bimestre",
                competencies = listOf(
                    CompetencyToggleRow(CompetencyId("PPSS-1"), 1, "Construye su identidad", true, 0),
                    CompetencyToggleRow(CompetencyId("PPSS-2"), 2, "Convive y participa democráticamente", false, 12),
                ),
                selectedCount = 1,
            ),
            onIntent = {},
        )
    }
}
