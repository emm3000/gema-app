package com.emm.gema.feature.attendance.month

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.attendance.AttendanceSiagieCode
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import java.time.YearMonth

@Composable
fun AttendanceMonthScreen(
    state: AttendanceMonthUiState,
    onIntent: (AttendanceMonthUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Asistencia · ${state.sectionTitle}",
                subtitle = "Resumen del mes",
                onBackClick = { onIntent(AttendanceMonthUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Exportar el mes",
                onClick = { onIntent(AttendanceMonthUiIntent.ExportClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canExport && !state.isExporting,
            )
        },
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
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
                MonthStepper(state = state, onIntent = onIntent)
            }
            if (state.rows.isNotEmpty()) {
                item {
                    AttendanceMonthHeader(modifier = Modifier.fillMaxWidth())
                }
            }
            if (state.rows.isEmpty() && !state.isLoading) {
                item {
                    GEmptyState(
                        title = "Todavía no hay asistencia este mes",
                        message = "Toma asistencia primero para ver el resumen mensual.",
                    )
                }
            }
            items(state.rows, key = { it.studentId.value }) { row: AttendanceMonthRow ->
                AttendanceMonthDataRow(row = row, modifier = Modifier.fillMaxWidth())
                HorizontalDivider()
            }
            item {
                GText(
                    text = "${state.recordedDayCount} días de clase registrados",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GemaSpacing.medium, vertical = GemaSpacing.medium),
                    style = GTextStyle.BODY_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AttendanceMonthHeader(modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(GemaSpacing.gridChipHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GText(
                text = "ALUMNO",
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = GemaSpacing.medium),
                style = GTextStyle.LABEL_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AttendanceStatus.entries.forEach { status: AttendanceStatus ->
                GText(
                    text = AttendanceSiagieCode.of(status),
                    modifier = Modifier.width(GemaSpacing.narrowCellWidth),
                    style = GTextStyle.LABEL_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun AttendanceMonthDataRow(row: AttendanceMonthRow, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(GemaSpacing.compactRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GText(
            text = row.displayName,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = GemaSpacing.medium),
            style = GTextStyle.BODY_MEDIUM,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        AttendanceStatus.entries.forEach { status: AttendanceStatus ->
            val count: Int = row.countsByStatus[status] ?: 0
            GText(
                text = count.toString(),
                modifier = Modifier.width(GemaSpacing.narrowCellWidth),
                style = GTextStyle.BODY_MEDIUM,
                color = if (count == 0) {
                    MaterialTheme.colorScheme.outlineVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun MonthStepper(
    state: AttendanceMonthUiState,
    onIntent: (AttendanceMonthUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCurrentMonth: Boolean = state.month != null && state.month == YearMonth.now()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Mes anterior",
            onClick = { onIntent(AttendanceMonthUiIntent.PreviousMonthClicked) },
        )
        GText(text = state.monthLabel, style = GTextStyle.TITLE_SMALL)
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Mes siguiente",
            onClick = { onIntent(AttendanceMonthUiIntent.NextMonthClicked) },
            isEnabled = !isCurrentMonth,
        )
    }
}

@PreviewLightDark
@Composable
private fun AttendanceMonthScreenPreview() {
    GemaTheme {
        AttendanceMonthScreen(
            state = AttendanceMonthUiState(
                isLoading = false,
                sectionTitle = "3° A",
                monthLabel = "setiembre 2026",
                recordedDayCount = 20,
                canExport = true,
                rows = listOf(
                    AttendanceMonthRow(
                        studentId = StudentId("1"),
                        displayName = "ACOSTA RIVERA, Luz M.",
                        countsByStatus = mapOf(
                            AttendanceStatus.PRESENT to 18,
                            AttendanceStatus.LATE to 1,
                            AttendanceStatus.ABSENT to 0,
                            AttendanceStatus.JUSTIFIED to 1,
                        ),
                    ),
                    AttendanceMonthRow(
                        studentId = StudentId("2"),
                        displayName = "BAUTISTA QUISPE, Jose",
                        countsByStatus = mapOf(
                            AttendanceStatus.PRESENT to 15,
                            AttendanceStatus.LATE to 2,
                            AttendanceStatus.ABSENT to 3,
                            AttendanceStatus.JUSTIFIED to 0,
                        ),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
