package com.emm.gema.feature.attendance.month

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.attendance.AttendanceSiagieCode
import com.emm.gema.core.domain.attendance.AttendanceStatus
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.label
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GMonthPickerDialog
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTableHeaderBand
import com.emm.gema.core.ui.GTableRow
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
    var isMonthPickerVisible: Boolean by remember { mutableStateOf(false) }

    GScreen(
        topBar = {
            GTopBar(
                title = "Asistencia · ${state.sectionTitle}",
                subtitle = "Resumen del mes",
                onBackClick = { onIntent(AttendanceMonthUiIntent.BackClicked) },
                actions = {
                    GIconButton(
                        icon = Icons.Filled.CalendarMonth,
                        contentDescription = "Elegir mes",
                        onClick = { isMonthPickerVisible = true },
                    )
                },
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Exportar el mes",
                onClick = { onIntent(AttendanceMonthUiIntent.ExportClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canExport && !state.isExporting,
                icon = Icons.Filled.Upload,
            )
        },
        contentGutter = false,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GemaSpacing.screenGutter),
                        tone = GBannerTone.ERROR,
                        actionText = "Entendido",
                        onActionClick = onMessageDismissed,
                    )
                }
            }
            item {
                MonthStepper(
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier.padding(horizontal = GemaSpacing.screenGutter),
                )
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
            }
            item {
                GText(
                    text = "${state.recordedDayCount} días de clase registrados",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.medium),
                    style = GTextStyle.BODY_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (isMonthPickerVisible) {
        GMonthPickerDialog(
            value = state.month ?: YearMonth.now(),
            onConfirm = { picked: YearMonth ->
                onIntent(AttendanceMonthUiIntent.MonthPicked(picked))
                isMonthPickerVisible = false
            },
            onDismiss = { isMonthPickerVisible = false },
            maximum = YearMonth.now(),
        )
    }
}

@Composable
private fun AttendanceMonthColumns(
    modifier: Modifier = Modifier,
    name: @Composable () -> Unit,
    counts: List<@Composable () -> Unit>,
) {
    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f).padding(horizontal = GemaSpacing.medium)) { name() }
        counts.forEach { cell: @Composable () -> Unit ->
            Box(modifier = Modifier.width(GemaSpacing.narrowCellWidth)) { cell() }
        }
    }
}

@Composable
private fun AttendanceMonthHeader(modifier: Modifier = Modifier) {
    GTableHeaderBand(modifier = modifier) {
        AttendanceMonthColumns(
            name = {
                GText(
                    text = "ALUMNO",
                    style = GTextStyle.LABEL_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            counts = AttendanceStatus.entries.map { status: AttendanceStatus ->
                {
                    GText(
                        text = AttendanceSiagieCode.of(status),
                        style = GTextStyle.LABEL_SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
    }
}

@Composable
private fun AttendanceMonthDataRow(row: AttendanceMonthRow, modifier: Modifier = Modifier) {
    GTableRow(modifier = modifier) {
        AttendanceMonthColumns(
            name = {
                GText(
                    text = row.displayName,
                    style = GTextStyle.BODY_MEDIUM,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            counts = AttendanceStatus.entries.map { status: AttendanceStatus ->
                val count: Int = row.countsByStatus[status] ?: 0
                {
                    GText(
                        text = count.toString(),
                        style = GTextStyle.BODY_MEDIUM,
                        color = if (count == 0) {
                            MaterialTheme.colorScheme.outlineVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            },
        )
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
        GText(text = state.month?.label().orEmpty(), style = GTextStyle.TITLE_SMALL)
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
                sectionTitle = "3ro A",
                month = YearMonth.of(2026, 9),
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
