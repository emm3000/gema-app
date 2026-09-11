package com.emm.gema.feature.sections.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar

@Composable
fun SectionDetailScreen(
    state: SectionDetailUiState,
    onIntent: (SectionDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = state.sectionTitle,
                onBackClick = { onIntent(SectionDetailUiIntent.BackClicked) },
                actions = {
                    GButton(
                        text = "Renombrar",
                        onClick = { onIntent(SectionDetailUiIntent.RenameClicked) },
                        variant = GButtonVariant.TEXT,
                    )
                },
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
        ) {
            GListItem(
                title = "Alumnos",
                modifier = Modifier.fillMaxWidth(),
                trailingText = state.studentCount.toString(),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.StudentsClicked) },
            )
            GListItem(
                title = "Niveles del periodo",
                modifier = Modifier.fillMaxWidth(),
                subtitle = state.currentPeriodLabel,
                trailingText = missingLabel(state.missingPeriodLevelCount),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.PeriodLevelsClicked) },
            )
            GListItem(
                title = "Áreas",
                modifier = Modifier.fillMaxWidth(),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.AreasClicked) },
            )
        }
    }
}

private fun missingLabel(missingPeriodLevelCount: Int): String? =
    "$missingPeriodLevelCount faltan".takeIf { missingPeriodLevelCount > 0 }

@PreviewLightDark
@Composable
private fun SectionDetailScreenPreview() {
    GemaTheme {
        SectionDetailScreen(
            state = SectionDetailUiState(
                isLoading = false,
                sectionTitle = "3° A",
                studentCount = 30,
                currentPeriodLabel = "II Bimestre",
                missingPeriodLevelCount = 12,
            ),
            onIntent = {},
        )
    }
}
