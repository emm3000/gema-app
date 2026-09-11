package com.emm.gema.feature.evaluation.worked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
                subtitle = "${state.areaName} - ${state.periodLabel}",
                onBackClick = { onIntent(WorkedCompetenciesUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            if (message != null) {
                item {
                    GBanner(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
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
                )
            }
            items(state.competencies, key = { it.id }) { row ->
                GCheckRow(
                    title = row.name,
                    isChecked = row.isWorked,
                    onCheckedChange = { onIntent(WorkedCompetenciesUiIntent.CompetencyToggled(row.id, it)) },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = row.siagieOrdinal.toSiagiePrefix(),
                    subtitle = recordedLevelsLabel(row.recordedLevelCount),
                )
            }
            item {
                GText(
                    text = "${state.selectedCount} de ${state.competencies.size} marcadas",
                    style = GTextStyle.BODY_MEDIUM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Int.toSiagiePrefix(): String = toString().padStart(2, '0')

private fun recordedLevelsLabel(recordedLevelCount: Int): String? =
    "$recordedLevelCount niveles registrados. Quedan guardados.".takeIf { recordedLevelCount > 0 }

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
                    CompetencyToggleRow("PPSS-1", 1, "Construye su identidad", true, 0),
                    CompetencyToggleRow("PPSS-2", 2, "Convive y participa democráticamente", false, 12),
                ),
                selectedCount = 1,
            ),
            onIntent = {},
        )
    }
}
