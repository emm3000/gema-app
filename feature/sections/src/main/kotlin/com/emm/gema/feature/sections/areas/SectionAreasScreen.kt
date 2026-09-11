package com.emm.gema.feature.sections.areas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.section.Area
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSwitchRow
import com.emm.gema.core.ui.GTopBar

@Composable
fun SectionAreasScreen(
    state: SectionAreasUiState,
    onIntent: (SectionAreasUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Áreas",
                subtitle = state.sectionTitle,
                onBackClick = { onIntent(SectionAreasUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(GemaSpacing.screenGutter),
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
                Text(
                    text = "Apaga las áreas que no dictas. Nada se borra: puedes volver a encenderlas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.areas, key = { it.id.name }) { row ->
                GSwitchRow(
                    title = row.name,
                    isChecked = row.isActive,
                    onCheckedChange = { onIntent(SectionAreasUiIntent.AreaToggled(row.id, it)) },
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = recordedLevelsLabel(row.recordedLevelCount),
                )
            }
        }
    }
}

private fun recordedLevelsLabel(recordedLevelCount: Int): String? =
    "$recordedLevelCount niveles registrados. Quedan guardados.".takeIf { recordedLevelCount > 0 }

@PreviewLightDark
@Composable
private fun SectionAreasScreenPreview() {
    GemaTheme {
        SectionAreasScreen(
            state = SectionAreasUiState(
                isLoading = false,
                sectionTitle = "3° A",
                areas = listOf(
                    AreaToggleRow(Area.COMU, "Comunicación", true, 0),
                    AreaToggleRow(Area.EFIS, "Educación Física", false, 12),
                ),
            ),
            onIntent = {},
        )
    }
}
