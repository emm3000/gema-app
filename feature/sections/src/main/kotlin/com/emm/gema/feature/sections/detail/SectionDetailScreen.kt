package com.emm.gema.feature.sections.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.sections.R

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
                        text = stringResource(R.string.sections_detail_rename),
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
            GButton(
                text = stringResource(R.string.sections_detail_take_attendance),
                onClick = { onIntent(SectionDetailUiIntent.TakeAttendanceClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GemaSpacing.screenGutter),
            )
            GListItem(
                title = stringResource(R.string.sections_detail_students),
                modifier = Modifier.fillMaxWidth(),
                trailingText = state.studentCount.toString(),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.StudentsClicked) },
            )
            GListItem(
                title = stringResource(R.string.sections_detail_attendance),
                modifier = Modifier.fillMaxWidth(),
                subtitle = state.todayLabel,
                trailingText = state.todayAttendanceSummary,
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.AttendanceClicked) },
            )
            GListItem(
                title = stringResource(R.string.sections_detail_period_levels),
                modifier = Modifier.fillMaxWidth(),
                subtitle = state.currentPeriodLabel,
                trailingText = missingLabel(state.missingPeriodLevelCount),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.PeriodLevelsClicked) },
            )
            GListItem(
                title = stringResource(R.string.sections_detail_export),
                modifier = Modifier.fillMaxWidth(),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.ExportClicked) },
            )
            GListItem(
                title = stringResource(R.string.sections_detail_activities),
                modifier = Modifier.fillMaxWidth(),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.ActivitiesClicked) },
            )
            GListItem(
                title = stringResource(R.string.sections_detail_areas),
                modifier = Modifier.fillMaxWidth(),
                hasChevron = true,
                onClick = { onIntent(SectionDetailUiIntent.AreasClicked) },
            )
        }
    }
}

@Composable
private fun missingLabel(missingPeriodLevelCount: Int): String? =
    pluralStringResource(R.plurals.sections_detail_missing_levels, missingPeriodLevelCount, missingPeriodLevelCount)
        .takeIf { missingPeriodLevelCount > 0 }

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
                todayLabel = "Jueves 10 de setiembre",
                todayAttendanceSummary = "Sin tomar",
            ),
            onIntent = {},
        )
    }
}
