package com.emm.gema.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GCalendarIconButton(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
) {
    var isPickerVisible: Boolean by remember { mutableStateOf(false) }

    GIconButton(
        icon = Icons.Filled.CalendarMonth,
        contentDescription = contentDescription,
        onClick = { isPickerVisible = true },
        modifier = modifier,
    )

    if (isPickerVisible) {
        val datePickerState: DatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value?.toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val candidate: LocalDate = utcTimeMillis.toLocalDateUtc()
                    val isAfterMin: Boolean = minDate == null || !candidate.isBefore(minDate)
                    val isBeforeMax: Boolean = maxDate == null || !candidate.isAfter(maxDate)
                    return isAfterMin && isBeforeMax
                }
            },
        )

        DatePickerDialog(
            onDismissRequest = { isPickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis: Long? = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            onValueChange(selectedMillis.toLocalDateUtc())
                        }
                        isPickerVisible = false
                    },
                ) {
                    Text(text = "Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { isPickerVisible = false }) {
                    Text(text = "Cancelar")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@PreviewLightDark
@Composable
private fun GCalendarIconButtonPreview() {
    GemaTheme {
        GCalendarIconButton(
            value = LocalDate.of(2026, 9, 10),
            onValueChange = {},
            contentDescription = "Elegir fecha",
        )
    }
}
