@file:Suppress("MatchingDeclarationName")

package com.emm.gema.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaTheme

data class GSegmentOption<T>(
    val value: T,
    val label: String,
    val contentDescription: String,
)

@Composable
fun <T> GSegmentedPicker(
    options: List<GSegmentOption<T>>,
    selected: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index: Int, option: GSegmentOption<T> ->
            val isSelected: Boolean = option.value == selected
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) null else option.value) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                    baseShape = GemaShapes.control,
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                enabled = isEnabled,
                modifier = Modifier.semantics { contentDescription = option.contentDescription },
            ) {
                Text(text = option.label)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GSegmentedPickerPreview() {
    GemaTheme {
        val options: List<GSegmentOption<String>> = listOf(
            GSegmentOption(value = "AD", label = "AD", contentDescription = "Logro destacado"),
            GSegmentOption(value = "A", label = "A", contentDescription = "Logro esperado"),
            GSegmentOption(value = "B", label = "B", contentDescription = "En proceso"),
            GSegmentOption(value = "C", label = "C", contentDescription = "En inicio"),
        )
        GSegmentedPicker(options = options, selected = "A", onSelect = {})
    }
}
