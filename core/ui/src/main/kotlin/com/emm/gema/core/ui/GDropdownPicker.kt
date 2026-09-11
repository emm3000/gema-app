@file:Suppress("MatchingDeclarationName")

package com.emm.gema.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

data class GPickerOption<T>(
    val value: T,
    val label: String,
    val badge: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GDropdownPicker(
    options: List<GPickerOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    isEnabled: Boolean = true,
) {
    var isExpanded: Boolean by remember { mutableStateOf(false) }
    val selectedOption: GPickerOption<T>? = options.find { it.value == selected }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it && isEnabled },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedOption?.label.orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .heightIn(min = GemaSpacing.minimumTouchTarget),
            enabled = isEnabled,
            readOnly = true,
            label = label?.let { { Text(text = it, style = MaterialTheme.typography.labelSmall) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            shape = GemaShapes.control,
            singleLine = true,
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { OptionRow(option) },
                    onClick = {
                        isExpanded = false
                        onSelect(option.value)
                    },
                )
            }
        }
    }
}

@Composable
private fun <T> OptionRow(option: GPickerOption<T>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (option.badge != null) {
            Text(
                text = option.badge,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GDropdownPickerPreview() {
    GemaTheme {
        GDropdownPicker(
            options = listOf(
                GPickerOption(value = "PPSS", label = "Personal Social"),
                GPickerOption(value = "MATE", label = "Matemática"),
            ),
            selected = "PPSS",
            onSelect = {},
            label = "Área",
        )
    }
}
