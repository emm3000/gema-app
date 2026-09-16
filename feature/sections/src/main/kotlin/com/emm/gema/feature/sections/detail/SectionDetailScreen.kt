package com.emm.gema.feature.sections.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.core.theme.GemaAccents
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.label
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GIcon
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GMenuAction
import com.emm.gema.core.ui.GOverflowMenu
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.sections.ATTENDANCE_UNTAKEN_LABEL
import com.emm.gema.feature.sections.R
import java.time.LocalDate
import java.util.Locale

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
                subtitle = sectionDetailSubtitle(state),
                onBackClick = { onIntent(SectionDetailUiIntent.BackClicked) },
                actions = {
                    GOverflowMenu(
                        actions = listOf(
                            GMenuAction(
                                label = stringResource(R.string.sections_detail_rename),
                                onClick = { onIntent(SectionDetailUiIntent.RenameClicked) },
                            ),
                            GMenuAction(
                                label = stringResource(R.string.sections_detail_areas),
                                onClick = { onIntent(SectionDetailUiIntent.AreasClicked) },
                            ),
                        ),
                        contentDescription = stringResource(R.string.sections_detail_overflow_description),
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
                .padding(top = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            if (state.hasStoredTemplate) {
                TemplateLoadedLine()
            }
            AttendanceCard(state = state, onIntent = onIntent)
            HubList(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun sectionDetailSubtitle(state: SectionDetailUiState): String {
    val students: String =
        pluralStringResource(R.plurals.sections_detail_subtitle_students, state.studentCount, state.studentCount)
    val period: String = state.currentPeriodLabel ?: return students
    return stringResource(R.string.sections_detail_subtitle_with_period, students, period)
}

@Composable
private fun todayEyebrowLabel(today: LocalDate?): String {
    val date: LocalDate = today ?: return ""
    val weekday: String = date.dayOfWeek.label().uppercase(Locale.ROOT)
    val month: String = date.month.label().uppercase(Locale.ROOT)
    return stringResource(R.string.sections_detail_attendance_today_label, weekday, date.dayOfMonth, month)
}

@Composable
private fun TemplateLoadedLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GIcon(
            icon = Icons.Filled.Check,
            tint = MaterialTheme.colorScheme.primary,
            size = 18.dp,
        )
        GText(
            text = stringResource(R.string.sections_detail_template_loaded),
            style = GTextStyle.BODY_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AttendanceCard(
    state: SectionDetailUiState,
    onIntent: (SectionDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(GemaShapes.container)
            .border(GemaBorder.hairline, MaterialTheme.colorScheme.outlineVariant, GemaShapes.container)
            .padding(GemaSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
    ) {
        GText(
            text = todayEyebrowLabel(state.today),
            style = GTextStyle.LABEL_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val isAttendanceUntaken: Boolean = state.todayAttendanceSummary == ATTENDANCE_UNTAKEN_LABEL
        GText(
            text = state.todayAttendanceSummary,
            style = GTextStyle.BODY_LARGE,
            color = if (isAttendanceUntaken) {
                GemaAccents.onWarningContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        GButton(
            text = stringResource(R.string.sections_detail_take_attendance),
            onClick = { onIntent(SectionDetailUiIntent.TakeAttendanceClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = GemaSpacing.small),
            icon = Icons.Filled.Check,
        )
    }
}

@Composable
private fun HubList(
    state: SectionDetailUiState,
    onIntent: (SectionDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val missingLevelsText: String? = missingLevelsTrailingText(state.missingPeriodLevelCount)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(GemaShapes.container)
            .border(GemaBorder.hairline, MaterialTheme.colorScheme.outlineVariant, GemaShapes.container),
    ) {
        GListItem(
            title = stringResource(R.string.sections_detail_students),
            leadingIcon = Icons.Filled.Group,
            trailingText = state.studentCount.toString(),
            hasChevron = true,
            onClick = { onIntent(SectionDetailUiIntent.StudentsClicked) },
        )
        GListItem(
            title = stringResource(R.string.sections_detail_attendance),
            leadingIcon = Icons.Filled.CalendarMonth,
            hasChevron = true,
            onClick = { onIntent(SectionDetailUiIntent.AttendanceClicked) },
        )
        GListItem(
            title = stringResource(R.string.sections_detail_period_levels),
            leadingIcon = Icons.AutoMirrored.Filled.Assignment,
            trailingText = missingLevelsText,
            trailingTextColor = GemaAccents.onWarningContainer,
            hasChevron = true,
            onClick = { onIntent(SectionDetailUiIntent.PeriodLevelsClicked) },
        )
        GListItem(
            title = stringResource(R.string.sections_detail_activities),
            leadingIcon = Icons.Filled.Checklist,
            trailingText = state.activityCount.toString(),
            hasChevron = true,
            onClick = { onIntent(SectionDetailUiIntent.ActivitiesClicked) },
        )
        GListItem(
            title = stringResource(R.string.sections_detail_export),
            leadingIcon = Icons.Filled.Upload,
            hasChevron = true,
            showDivider = false,
            onClick = { onIntent(SectionDetailUiIntent.ExportClicked) },
        )
    }
}

@Composable
private fun missingLevelsTrailingText(missingPeriodLevelCount: Int): String? {
    if (missingPeriodLevelCount <= 0) return null
    return pluralStringResource(
        R.plurals.sections_detail_missing_levels,
        missingPeriodLevelCount,
        missingPeriodLevelCount,
    )
}

@PreviewLightDark
@Composable
private fun SectionDetailScreenPreview() {
    GemaTheme {
        SectionDetailScreen(
            state = SectionDetailUiState(
                isLoading = false,
                sectionTitle = "3ro A",
                studentCount = 30,
                currentPeriodLabel = "II Bimestre",
                hasStoredTemplate = true,
                missingPeriodLevelCount = 12,
                activityCount = 5,
                today = LocalDate.of(2026, 9, 10),
                todayAttendanceSummary = "Sin tomar",
            ),
            onIntent = {},
        )
    }
}
