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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.feature.attendance.asStatus
import com.emm.gema.feature.attendance.asToggleOption
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GAttendanceOption
import com.emm.gema.core.ui.GAttendanceToggle
import com.emm.gema.core.ui.gAttendanceRowColor
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar
import java.time.LocalDate

private const val SAVES_ITSELF_SUBTITLE: String = "Cada toque se guarda solo"

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
                subtitle = SAVES_ITSELF_SUBTITLE,
                onBackClick = { onIntent(AttendanceDayUiIntent.BackClicked) },
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
            items(state.rows, key = { it.studentId }) { row: AttendanceRow ->
                StudentRow(row = row, onIntent = onIntent)
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
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Día anterior",
            onClick = { onIntent(AttendanceDayUiIntent.PreviousDayClicked) },
        )
        GDateField(
            value = state.date,
            onValueChange = { picked: LocalDate -> onIntent(AttendanceDayUiIntent.DatePicked(picked)) },
            label = state.dateLabel,
            modifier = Modifier.weight(1f),
            maxDate = LocalDate.now(),
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
    GCard(modifier = modifier) {
        GListItem(
            title = "${state.presentCount} de ${state.totalCount} presentes",
            modifier = Modifier.fillMaxWidth(),
            subtitle = "${state.unmarkedCount} sin marcar",
            trailing = {
                GButton(
                    text = "Todos presentes",
                    onClick = { onIntent(AttendanceDayUiIntent.MarkAllPresent) },
                    variant = GButtonVariant.TEXT,
                    enabled = state.canMarkAllPresent,
                )
            },
        )
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
            .padding(vertical = GemaSpacing.extraSmall),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
    ) {
        GListItem(
            title = row.displayName,
            modifier = Modifier.fillMaxWidth(),
            trailingText = "sin marcar".takeIf { !row.isRecorded },
        )
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
                sectionTitle = "3° A",
                date = LocalDate.of(2026, 9, 10),
                dateLabel = "Jue 10 set 2026",
                presentCount = 2,
                totalCount = 3,
                unmarkedCount = 1,
                rows = listOf(
                    AttendanceRow("1", "ACOSTA RIVERA, Luz Maria", AttendanceStatus.PRESENT, true),
                    AttendanceRow("2", "BAUTISTA QUISPE, Jose", AttendanceStatus.LATE, true),
                    AttendanceRow("3", "CCAHUANA MAMANI, Rosa", AttendanceStatus.PRESENT, false),
                ),
            ),
            onIntent = {},
        )
    }
}
