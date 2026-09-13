@file:Suppress("MatchingDeclarationName")

package com.emm.gema.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme

data class GChoiceChipOption<T>(
    val value: T,
    val label: String,
    val contentDescription: String,
)

@Composable
fun <T> GChoiceChipRow(
    options: List<GChoiceChipOption<T>>,
    selected: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        options.forEach { option: GChoiceChipOption<T> ->
            val isSelected: Boolean = option.value == selected
            val containerColor: androidx.compose.ui.graphics.Color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
            Surface(
                onClick = { onSelect(if (isSelected) null else option.value) },
                modifier = Modifier
                    .weight(1f)
                    .height(GemaSpacing.minimumTouchTarget)
                    .semantics { contentDescription = option.contentDescription },
                shape = GemaShapes.control,
                color = containerColor,
                border = BorderStroke(
                    width = if (isSelected) GemaSpacing.indicatorStroke else GemaBorder.hairline,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GemaSpacing.minimumTouchTarget),
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GChoiceChipRowPreview() {
    GemaTheme {
        val options: List<GChoiceChipOption<Int>> = (1..6).map {
            GChoiceChipOption(value = it, label = "$it", contentDescription = "Grado $it")
        }
        GChoiceChipRow(options = options, selected = 3, onSelect = {})
    }
}
