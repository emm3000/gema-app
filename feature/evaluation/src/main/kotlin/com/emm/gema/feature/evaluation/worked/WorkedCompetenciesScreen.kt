package com.emm.gema.feature.evaluation.worked

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GCheckRow
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar

@Composable
fun WorkedCompetenciesScreen(
    state: WorkedCompetenciesUiState,
    onIntent: (WorkedCompetenciesUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Competencias trabajadas",
                subtitle = "${state.areaName} · ${state.periodLabel}",
                onBackClick = { onIntent(WorkedCompetenciesUiIntent.BackClicked) },
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
                        actionText = "Entendido",
                        onActionClick = onMessageDismissed,
                    )
                }
            }
            item {
                GText(
                    text = "Marca solo las competencias que trabajaste. Solo esas se exportan.",
                    style = GTextStyle.BODY_MEDIUM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.small),
                )
            }
            if (state.competencies.isNotEmpty()) {
                item {
                    val dividerColor: Color = MaterialTheme.colorScheme.outlineVariant
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    color = dividerColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = GemaBorder.hairline.toPx(),
                                )
                            },
                    )
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
    recordedLevelsWarning: String?,
) {
    val topBorderColor: Color = MaterialTheme.colorScheme.outlineVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = topBorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = GemaBorder.hairline.toPx(),
                )
            }
            .padding(top = GemaSpacing.medium, bottom = GemaSpacing.small),
    ) {
        GText(
            text = "$selectedCount de $totalCount marcadas",
            style = GTextStyle.LABEL_MEDIUM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (recordedLevelsWarning != null) {
            GBanner(
                text = recordedLevelsWarning,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = GemaSpacing.small),
                tone = GBannerTone.WARNING,
                icon = Icons.Filled.Warning,
            )
        }
    }
}

private fun Int.toSiagiePrefix(): String = toString().padStart(2, '0')

private fun List<CompetencyToggleRow>.recordedLevelsWarning(): String? {
    val withRecordedLevels: List<CompetencyToggleRow> = filter { it.recordedLevelCount > 0 }
    if (withRecordedLevels.isEmpty()) return null
    val ordinals: String = withRecordedLevels.joinToString(", ") { it.siagieOrdinal.toSiagiePrefix() }
    val prefix: String = if (withRecordedLevels.size == 1) "La" else "Las"
    val verb: String = if (withRecordedLevels.size == 1) "tiene" else "tienen"
    return "$prefix $ordinals $verb niveles registrados. Quedan guardados y dejan de exportarse."
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
