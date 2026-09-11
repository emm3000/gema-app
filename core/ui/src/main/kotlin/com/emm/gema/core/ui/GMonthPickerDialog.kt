package com.emm.gema.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.abbreviatedLabel
import java.time.Month
import java.time.YearMonth

private const val MONTHS_PER_ROW: Int = 3

@Composable
fun GMonthPickerDialog(
    value: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minimum: YearMonth? = null,
    maximum: YearMonth? = null,
) {
    var displayedYear: Int by remember { mutableIntStateOf(value.year) }
    var selected: YearMonth by remember { mutableStateOf(value) }

    GDialog(
        title = "Elige el mes",
        confirmText = "Aceptar",
        onConfirm = { onConfirm(selected) },
        onDismiss = onDismiss,
        modifier = modifier,
        dismissText = "Cancelar",
    ) {
        YearStepper(
            year = displayedYear,
            canGoToPreviousYear = minimum == null || displayedYear - 1 >= minimum.year,
            canGoToNextYear = maximum == null || displayedYear + 1 <= maximum.year,
            onPreviousYear = { displayedYear -= 1 },
            onNextYear = { displayedYear += 1 },
        )
        MonthGrid(
            year = displayedYear,
            selected = selected,
            minimum = minimum,
            maximum = maximum,
            onMonthSelected = { selected = it },
        )
    }
}

@Composable
private fun YearStepper(
    year: Int,
    canGoToPreviousYear: Boolean,
    canGoToNextYear: Boolean,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Año anterior",
            onClick = onPreviousYear,
            isEnabled = canGoToPreviousYear,
        )
        GText(text = year.toString(), style = GTextStyle.TITLE_SMALL)
        GIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Año siguiente",
            onClick = onNextYear,
            isEnabled = canGoToNextYear,
        )
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    selected: YearMonth,
    minimum: YearMonth?,
    maximum: YearMonth?,
    onMonthSelected: (YearMonth) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
        Month.entries.chunked(MONTHS_PER_ROW).forEachIndexed { rowIndex: Int, rowMonths: List<Month> ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
            ) {
                rowMonths.forEachIndexed { columnIndex: Int, monthOfYear: Month ->
                    val monthValue: Int = rowIndex * MONTHS_PER_ROW + columnIndex + 1
                    val month: YearMonth = YearMonth.of(year, monthValue)
                    val isInBounds: Boolean = (minimum == null || !month.isBefore(minimum)) &&
                        (maximum == null || !month.isAfter(maximum))
                    MonthChip(
                        name = monthOfYear.abbreviatedLabel(),
                        isSelected = month == selected,
                        isEnabled = isInBounds,
                        onClick = { onMonthSelected(month) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthChip(
    name: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor: Color = when {
        !isEnabled -> MaterialTheme.colorScheme.outlineVariant
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val border: BorderStroke? = if (isSelected) {
        null
    } else {
        BorderStroke(GemaBorder.hairline, MaterialTheme.colorScheme.outline)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(GemaSpacing.minimumTouchTarget),
        enabled = isEnabled,
        shape = GemaShapes.control,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = border,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GMonthPickerDialogPreview() {
    GemaTheme {
        GMonthPickerDialog(
            value = YearMonth.of(2026, 9),
            onConfirm = {},
            onDismiss = {},
            maximum = YearMonth.of(2026, 9),
        )
    }
}
