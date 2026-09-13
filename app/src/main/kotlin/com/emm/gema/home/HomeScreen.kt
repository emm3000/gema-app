package com.emm.gema.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.R
import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.dayMonthLabel
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerActionStyle
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GBorderedContainer
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GCircledIcon
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GExtendedFab
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GMenuAction
import com.emm.gema.core.ui.GOverflowMenu
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import java.time.LocalDate

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "",
                titleContent = { HomeTitle(yearLabel = state.schoolYearLabel) },
                actions = {
                    GIconButton(
                        icon = Icons.Filled.Download,
                        contentDescription = stringResource(R.string.home_backup_reminder_action),
                        onClick = { onIntent(HomeUiIntent.BackupReminderClicked) },
                    )
                    GOverflowMenu(
                        actions = listOf(
                            GMenuAction(
                                label = stringResource(R.string.home_switch_school_year),
                                onClick = { onIntent(HomeUiIntent.SchoolYearSwitcherClicked) },
                            ),
                        ),
                        contentDescription = stringResource(R.string.home_more_options),
                    )
                },
            )
        },
        fab = {
            GExtendedFab(
                text = stringResource(R.string.home_add_section),
                icon = Icons.Filled.Add,
                onClick = { onIntent(HomeUiIntent.AddSectionClicked) },
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
            if (state.currentPeriodLabel != null) {
                item {
                    CurrentPeriodCard(
                        label = state.currentPeriodLabel,
                        daysLeft = state.daysLeftInPeriod,
                        endDate = state.currentPeriodEndDate,
                    )
                }
            }
            val reminder: BackupReminder? = state.backupReminder
            if (reminder != null) {
                item {
                    GBanner(
                        text = reminderText(reminder),
                        modifier = Modifier.fillMaxWidth(),
                        tone = GBannerTone.ERROR,
                        icon = Icons.Filled.Warning,
                        actionText = stringResource(R.string.home_backup_reminder_action),
                        onActionClick = { onIntent(HomeUiIntent.BackupReminderClicked) },
                        actionStyle = GBannerActionStyle.LINK,
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
            if (state.sections.isNotEmpty()) {
                item {
                    GText(
                        text = stringResource(R.string.home_sections_label),
                        style = GTextStyle.LABEL_SMALL_EMPHASIS,
                    )
                }
            }
            items(state.sections, key = { it.id.value }) { row ->
                SectionCard(row = row, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun HomeTitle(yearLabel: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GText(text = stringResource(R.string.app_name), style = GTextStyle.TITLE_MEDIUM_EMPHASIS)
        GText(
            text = yearLabel,
            style = GTextStyle.LABEL_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CurrentPeriodCard(label: String, daysLeft: Int?, endDate: LocalDate?) {
    GCard(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall)) {
                GText(
                    text = label,
                    style = GTextStyle.BODY_LARGE,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (daysLeft != null && endDate != null) {
                    GText(
                        text = pluralStringResource(
                            R.plurals.home_period_days_left,
                            daysLeft,
                            daysLeft,
                            endDate.dayMonthLabel(),
                        ),
                        style = GTextStyle.BODY_SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            GCircledIcon(
                icon = Icons.Filled.CalendarMonth,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

internal fun sectionCardTestTag(id: SectionId): String = "home_section_card_${id.value}"

@Composable
private fun SectionCard(row: SectionRow, onIntent: (HomeUiIntent) -> Unit, modifier: Modifier = Modifier) {
    GBorderedContainer(modifier = modifier) {
        GListItem(
            title = row.title,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(sectionCardTestTag(row.id)),
            titleStyle = GTextStyle.NUMERAL,
            trailingText = pluralStringResource(R.plurals.home_section_students, row.studentCount, row.studentCount),
            showDivider = false,
            onClick = { onIntent(HomeUiIntent.SectionClicked(row.id)) },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GemaSpacing.medium)
                .padding(bottom = GemaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
        ) {
            GText(
                text = stringResource(R.string.home_today_status, attendanceLabel(row.attendance)),
                style = GTextStyle.BODY_LARGE,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (row.attendance.isTaken) {
                GButton(
                    text = stringResource(R.string.home_view_attendance),
                    onClick = { onIntent(HomeUiIntent.TakeAttendanceClicked(row.id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = GemaSpacing.small),
                    variant = GButtonVariant.SECONDARY,
                )
            } else {
                GButton(
                    text = stringResource(R.string.home_take_attendance),
                    onClick = { onIntent(HomeUiIntent.TakeAttendanceClicked(row.id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = GemaSpacing.small),
                    icon = Icons.Filled.Check,
                )
            }
        }
    }
}

@Composable
private fun attendanceLabel(summary: AttendanceDaySummary): String = if (summary.isTaken) {
    stringResource(R.string.home_attendance_taken, summary.presentCount, summary.totalCount)
} else {
    stringResource(R.string.home_attendance_untaken)
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
                schoolYearId = SchoolYearId("2026"),
                schoolYearLabel = "2026",
                currentPeriodLabel = "II Bimestre",
                daysLeftInPeriod = 24,
                currentPeriodEndDate = LocalDate.of(2026, 7, 31),
                sections = listOf(
                    SectionRow(SectionId("a"), "3ro A", 30),
                    SectionRow(SectionId("b"), "4to B", 28),
                ),
            ),
            onIntent = {},
        )
    }
}
