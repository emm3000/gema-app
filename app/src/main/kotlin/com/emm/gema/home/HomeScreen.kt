package com.emm.gema.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.R
import com.emm.gema.core.domain.attendance.AttendanceDaySummary
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.theme.GemaAccents
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.dayMonthLabel
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerActionStyle
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GDivider
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

internal const val HOME_PRIMARY_ACTION_TEST_TAG: String = "home_primary_action"

internal fun sectionRowTestTag(id: SectionId): String = "home_section_row_${id.value}"

@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstPendingId: SectionId? = state.sections.firstOrNull { !it.attendance.isTaken }?.id
    val gutter: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.extraSmall)
    val listState: LazyListState = rememberLazyListState()

    GScreen(
        topBar = {
            HomeTopBar(
                yearLabel = state.schoolYearLabel,
                onIntent = onIntent,
                isContentScrolled = listState.canScrollBackward,
            )
        },
        fab = {
            GExtendedFab(
                text = stringResource(R.string.home_add_section),
                icon = Icons.Filled.Add,
                onClick = { onIntent(HomeUiIntent.AddSectionClicked) },
            )
        },
        contentGutter = false,
        modifier = modifier,
    ) { padding: PaddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.currentPeriodLabel != null) {
                item {
                    CurrentPeriodRow(
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
                        modifier = gutter,
                        tone = GBannerTone.ERROR,
                        hasLeadingDot = true,
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
                        modifier = gutter,
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
                        modifier = gutter,
                        actionLabel = stringResource(R.string.home_add_section),
                        onActionClick = { onIntent(HomeUiIntent.AddSectionClicked) },
                    )
                }
            }
            if (state.sections.isNotEmpty()) {
                item {
                    GText(
                        text = state.todayLabel,
                        modifier = Modifier.padding(
                            start = GemaSpacing.screenGutter,
                            end = GemaSpacing.screenGutter,
                            top = GemaSpacing.large,
                            bottom = GemaSpacing.small,
                        ),
                        style = GTextStyle.LABEL_SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.sections, key = { it.id.value }) { row ->
                if (row.attendance.isTaken) {
                    TakenSectionRow(row = row, onIntent = onIntent)
                } else {
                    PendingSectionRow(row = row, isPrimary = row.id == firstPendingId, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(yearLabel: String, onIntent: (HomeUiIntent) -> Unit, isContentScrolled: Boolean) {
    GTopBar(
        title = "",
        titleContent = { HomeTitle(yearLabel = yearLabel) },
        isContentScrolled = isContentScrolled,
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
private fun CurrentPeriodRow(label: String, daysLeft: Int?, endDate: LocalDate?) {
    GListItem(
        title = label,
        modifier = Modifier.fillMaxWidth(),
        titleStyle = GTextStyle.TITLE_MEDIUM,
        subtitle = if (daysLeft != null && endDate != null) {
            pluralStringResource(R.plurals.home_period_days_left, daysLeft, daysLeft, endDate.dayMonthLabel())
        } else {
            null
        },
        leadingIcon = Icons.Filled.CalendarMonth,
        showDivider = false,
    )
}

@Composable
private fun TakenSectionRow(row: SectionRow, onIntent: (HomeUiIntent) -> Unit) {
    GListItem(
        title = row.title,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(sectionRowTestTag(row.id)),
        titleStyle = GTextStyle.NUMERAL,
        subtitle = statusLine(row),
        subtitleColor = statusColor(row),
        trailingText = studentCountLabel(row.studentCount),
        hasChevron = true,
        onClick = { onIntent(HomeUiIntent.SectionClicked(row.id)) },
    )
}

@Composable
private fun PendingSectionRow(row: SectionRow, isPrimary: Boolean, onIntent: (HomeUiIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GListItem(
            title = row.title,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(sectionRowTestTag(row.id)),
            titleStyle = GTextStyle.NUMERAL,
            subtitle = statusLine(row),
            subtitleColor = statusColor(row),
            trailingText = studentCountLabel(row.studentCount),
            showDivider = false,
            onClick = { onIntent(HomeUiIntent.SectionClicked(row.id)) },
        )
        GButton(
            text = stringResource(R.string.home_take_attendance),
            onClick = { onIntent(HomeUiIntent.TakeAttendanceClicked(row.id)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = GemaSpacing.screenGutter, end = GemaSpacing.screenGutter, bottom = GemaSpacing.medium)
                .then(if (isPrimary) Modifier.testTag(HOME_PRIMARY_ACTION_TEST_TAG) else Modifier),
            variant = if (isPrimary) GButtonVariant.PRIMARY else GButtonVariant.SECONDARY,
        )
        GDivider()
    }
}

@Composable
private fun studentCountLabel(studentCount: Int): String =
    pluralStringResource(R.plurals.home_section_students, studentCount, studentCount)

@Composable
private fun statusLine(row: SectionRow): String {
    val attendance: String = attendanceLabel(row.attendance)
    val missingLevelCount: Int = row.missingLevelCount ?: 0
    if (missingLevelCount <= 0) return attendance
    return pluralStringResource(R.plurals.home_status_missing_levels, missingLevelCount, attendance, missingLevelCount)
}

@Composable
private fun statusColor(row: SectionRow): Color {
    val hasPending: Boolean = !row.attendance.isTaken || (row.missingLevelCount ?: 0) > 0
    return if (hasPending) GemaAccents.onWarningContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
                todayLabel = "HOY · MARTES 10 DE SETIEMBRE",
                currentPeriodLabel = "II Bimestre",
                daysLeftInPeriod = 24,
                currentPeriodEndDate = LocalDate.of(2026, 7, 31),
                sections = listOf(
                    SectionRow(SectionId("a"), "3ro A", 30, missingLevelCount = 12),
                    SectionRow(SectionId("b"), "4to B", 27, AttendanceDaySummary(25, 27, 0), 12),
                ),
            ),
            onIntent = {},
        )
    }
}
