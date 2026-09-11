package com.emm.gema.feature.attendance.month

import androidx.compose.foundation.layout.Arrangement
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
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar

private const val COLUMN_HEADER: String = "P      T      F      FJ"

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
                subtitle = state.monthLabel,
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
                    GListItem(title = "", modifier = Modifier.fillMaxWidth(), trailingText = COLUMN_HEADER)
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
            items(state.rows, key = { it.studentId }) { row: AttendanceMonthRow ->
                GListItem(
                    title = row.displayName,
                    modifier = Modifier.fillMaxWidth(),
                    trailingText = row.countsLabel(),
                )
            }
            item {
                GListItem(
                    title = "${state.recordedDayCount} días de clase registrados",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun AttendanceMonthRow.countsLabel(): String =
    "$presentCount      $lateCount      $absentCount      $justifiedCount"

@Composable
private fun MonthStepper(
    state: AttendanceMonthUiState,
    onIntent: (AttendanceMonthUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Mes anterior",
            onClick = { onIntent(AttendanceMonthUiIntent.PreviousMonthClicked) },
        )
        GListItem(title = state.monthLabel, modifier = Modifier.weight(1f))
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Mes siguiente",
            onClick = { onIntent(AttendanceMonthUiIntent.NextMonthClicked) },
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
                    AttendanceMonthRow("1", "ACOSTA RIVERA, Luz M.", 18, 1, 0, 1),
                    AttendanceMonthRow("2", "BAUTISTA QUISPE, Jose", 15, 2, 3, 0),
                ),
            ),
            onIntent = {},
        )
    }
}
