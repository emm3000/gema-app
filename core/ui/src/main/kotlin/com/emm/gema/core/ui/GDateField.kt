package com.emm.gema.core.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val gDateFieldFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GDateField(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    isEnabled: Boolean = true,
) {
    var isPickerVisible: Boolean by remember { mutableStateOf(false) }
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    if (isEnabled) {
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Release) {
                    isPickerVisible = true
                }
            }
        }
    }

    val displayValue: String = value?.format(gDateFieldFormatter).orEmpty()
    val supportingContent: (@Composable () -> Unit)? = errorText?.let { text ->
        {
            Text(text = text, color = MaterialTheme.colorScheme.error)
        }
    }

    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        modifier = modifier,
        enabled = isEnabled,
        readOnly = true,
        label = { Text(text = label) },
        isError = errorText != null,
        supportingText = supportingContent,
        interactionSource = interactionSource,
        shape = GemaShapes.control,
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
private fun GDateFieldPreview() {
    val previewYear: Int = 2026
    val previewMonth: Int = 3
    val previewDay: Int = 2

    GemaTheme {
        GDateField(
            value = LocalDate.of(previewYear, previewMonth, previewDay),
            onValueChange = {},
            label = "Fecha de inicio",
        )
    }
}
