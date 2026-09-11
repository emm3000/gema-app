package com.emm.gema.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.R
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = state.schoolYearLabel,
                subtitle = state.currentPeriodLabel,
                actions = {
                    GIconButton(
                        icon = Icons.Filled.DateRange,
                        contentDescription = stringResource(R.string.home_switch_school_year),
                        onClick = { onIntent(HomeUiIntent.SchoolYearSwitcherClicked) },
                    )
                    GIconButton(
                        icon = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.home_add_section),
                        onClick = { onIntent(HomeUiIntent.AddSectionClicked) },
                    )
                },
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
            val reminder: BackupReminder? = state.backupReminder
            if (reminder != null) {
                item {
                    GBanner(
                        text = reminderText(reminder),
                        modifier = Modifier.fillMaxWidth(),
                        tone = GBannerTone.WARNING,
                        actionText = stringResource(R.string.home_backup_reminder_action),
                        onActionClick = { onIntent(HomeUiIntent.BackupReminderClicked) },
                    )
                }
            }
            if (state.currentPeriodLabel == null && state.schoolYearId != null) {
                item {
                    GBanner(
                        text = stringResource(R.string.home_out_of_period),
                        modifier = Modifier.fillMaxWidth(),
                        tone = GBannerTone.WARNING,
                        actionText = stringResource(R.string.home_review_periods),
                        onActionClick = { onIntent(HomeUiIntent.OutOfPeriodClicked) },
                    )
                }
            }
            if (state.sections.isEmpty() && !state.isLoading) {
                item {
                    GEmptyState(
                        title = stringResource(R.string.home_empty_title),
                        message = stringResource(R.string.home_empty_message),
                        actionLabel = stringResource(R.string.home_add_section),
                        onActionClick = { onIntent(HomeUiIntent.AddSectionClicked) },
                    )
                }
            }
            items(state.sections, key = { it.id }) { row ->
                GListItem(
                    title = row.title,
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = pluralStringResource(
                        R.plurals.home_section_students,
                        row.studentCount,
                        row.studentCount,
                    ),
                    hasChevron = true,
                    onClick = { onIntent(HomeUiIntent.SectionClicked(row.id)) },
                )
            }
        }
    }
}

@Composable
private fun reminderText(reminder: BackupReminder): String = if (reminder.hasEverBackedUp) {
    pluralStringResource(
        R.plurals.home_backup_reminder_days,
        reminder.daysSinceLastBackup,
        reminder.daysSinceLastBackup,
    )
} else {
    stringResource(R.string.home_backup_reminder_never)
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    GemaTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                schoolYearId = "2026",
                schoolYearLabel = "2026",
                currentPeriodLabel = "II Bimestre",
                sections = listOf(SectionRow("a", "3° A", 30), SectionRow("b", "4° B", 28)),
            ),
            onIntent = {},
        )
    }
}
