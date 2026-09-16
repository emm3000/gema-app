package com.emm.gema.core.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.asDayMonthYear
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
fun GDateField(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    isEnabled: Boolean = true,
    contentDescription: String? = null,
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

    val displayValue: String = value?.asDayMonthYear().orEmpty()
    val supportingContent: (@Composable () -> Unit)? = errorText?.let { text ->
        {
            Text(text = text, color = MaterialTheme.colorScheme.error)
        }
    }
    val fieldModifier: Modifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }
    val labelContent: (@Composable () -> Unit)? = label?.let { text ->
        {
            val labelModifier: Modifier = if (contentDescription != null) {
                Modifier.clearAndSetSemantics {}
            } else {
                Modifier
            }
            Text(text = text, modifier = labelModifier)
        }
    }

    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        modifier = fieldModifier,
        enabled = isEnabled,
        readOnly = true,
        label = labelContent,
        isError = isError,
        supportingText = supportingContent,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        interactionSource = interactionSource,
        shape = GemaShapes.control,
        singleLine = false,
        minLines = 1,
        maxLines = 2,
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

@PreviewLightDark
@Composable
private fun GDateFieldWithContentDescriptionPreview() {
    val previewYear: Int = 2026
    val previewMonth: Int = 3
    val previewDay: Int = 2

    GemaTheme {
        GDateField(
            value = LocalDate.of(previewYear, previewMonth, previewDay),
            onValueChange = {},
            label = "Inicio",
            contentDescription = "Inicio del I Bimestre",
        )
    }
}
