package com.emm.gema.feature.attendance.day

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.label
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.label
import com.emm.gema.core.ui.GAttendanceOption
import com.emm.gema.core.ui.GAttendanceToggle
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCalendarIconButton
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.ui.gAttendanceRowColor
import com.emm.gema.feature.attendance.R
import com.emm.gema.feature.attendance.asStatus
import com.emm.gema.feature.attendance.asToggleOption
import java.time.LocalDate

@Composable
fun AttendanceDayScreen(
    state: AttendanceDayUiState,
    onIntent: (AttendanceDayUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Asistencia · ${state.sectionTitle}",
                subtitle = stringResource(R.string.attendance_day_subtitle_saves_itself),
                onBackClick = { onIntent(AttendanceDayUiIntent.BackClicked) },
                actions = {
                    GCalendarIconButton(
                        value = state.date,
                        onValueChange = { picked: LocalDate -> onIntent(AttendanceDayUiIntent.DatePicked(picked)) },
                        contentDescription = "Elegir fecha",
                        maxDate = LocalDate.now(),
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
            contentPadding = PaddingValues(vertical = GemaSpacing.small),
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
                DayStepper(state = state, onIntent = onIntent)
            }
            item {
                DaySummary(state = state, onIntent = onIntent)
            }
            if (state.rows.isEmpty() && !state.isLoading) {
                item {
                    GEmptyState(
                        title = "Todavía no hay alumnos",
                        message = "Agrega a los alumnos de la sección para tomar asistencia.",
                    )
                }
            }
            items(state.rows, key = { it.studentId.value }) { row: AttendanceRow ->
                StudentRow(row = row, onIntent = onIntent)
            }
            item {
                GListItem(
                    title = "Resumen del mes",
                    modifier = Modifier.fillMaxWidth(),
                    hasChevron = true,
                    onClick = { onIntent(AttendanceDayUiIntent.MonthlySummaryClicked) },
                )
            }
        }
    }
}

@Composable
private fun DayStepper(
    state: AttendanceDayUiState,
    onIntent: (AttendanceDayUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Día anterior",
            onClick = { onIntent(AttendanceDayUiIntent.PreviousDayClicked) },
        )
        GText(
            text = state.date?.label().orEmpty(),
            style = GTextStyle.TITLE_MEDIUM,
        )
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Día siguiente",
            onClick = { onIntent(AttendanceDayUiIntent.NextDayClicked) },
            isEnabled = state.canGoForward,
        )
    }
}

@Composable
private fun DaySummary(
    state: AttendanceDayUiState,
    onIntent: (AttendanceDayUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GCard(modifier = modifier, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
                ) {
                    GText(text = "${state.presentCount}", style = GTextStyle.TITLE_MEDIUM_EMPHASIS)
                    GText(
                        text = "de ${state.totalCount} presentes",
                        style = GTextStyle.BODY_LARGE,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GText(
                    text = "${state.unmarkedCount} sin marcar",
                    style = GTextStyle.BODY_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GButton(
                text = "Todos presentes",
                onClick = { onIntent(AttendanceDayUiIntent.MarkAllPresent) },
                variant = GButtonVariant.SECONDARY,
                enabled = state.canMarkAllPresent,
            )
        }
    }
}

@Composable
private fun StudentRow(
    row: AttendanceRow,
    onIntent: (AttendanceDayUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(gAttendanceRowColor(row.isRecorded))
            .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.small),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GText(text = row.displayName, style = GTextStyle.BODY_LARGE)
            if (!row.isRecorded) {
                GText(
                    text = "sin marcar",
                    style = GTextStyle.LABEL_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        GAttendanceToggle(
            option = row.status.asToggleOption(),
            isRecorded = row.isRecorded,
            onSelect = { option: GAttendanceOption ->
                onIntent(AttendanceDayUiIntent.StatusSelected(row.studentId, option.asStatus()))
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewLightDark
@Composable
private fun AttendanceDayScreenPreview() {
    GemaTheme {
        AttendanceDayScreen(
            state = AttendanceDayUiState(
                isLoading = false,
                sectionTitle = "${Grade.THIRD.label()} A",
                date = LocalDate.of(2026, 9, 10),
                presentCount = 2,
                totalCount = 3,
                unmarkedCount = 1,
                rows = listOf(
                    AttendanceRow(StudentId("1"), "ACOSTA RIVERA, Luz Maria", AttendanceStatus.PRESENT, true),
                    AttendanceRow(StudentId("2"), "BAUTISTA QUISPE, Jose", AttendanceStatus.LATE, true),
                    AttendanceRow(StudentId("3"), "CCAHUANA MAMANI, Rosa", AttendanceStatus.PRESENT, false),
                ),
            ),
            onIntent = {},
        )
    }
}
