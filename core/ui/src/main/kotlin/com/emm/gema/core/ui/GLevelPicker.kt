package com.emm.gema.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaTheme

private const val NO_LEVEL_LABEL: String = "—"
private const val NO_LEVEL_DESCRIPTION: String = "Sin nivel"

@Composable
fun GLevelPicker(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val options: List<GSegmentOption<String?>> = GLevelOption.entries.map { option ->
        GSegmentOption<String?>(
            value = option.letter,
            label = option.letter,
            contentDescription = option.contentDescription,
        )
    } + GSegmentOption<String?>(
        value = null,
        label = NO_LEVEL_LABEL,
        contentDescription = NO_LEVEL_DESCRIPTION,
    )

    GSegmentedPicker(
        options = options,
        selected = selected,
        onSelect = onSelect,
        modifier = modifier,
        isEnabled = isEnabled,
        activeContentColor = MaterialTheme.colorScheme.onSurface,
        activeBorderColor = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun GLevelPicker(
    selected: String?,
    onSelect: (String) -> Unit,
    noEvidenceValue: String,
    noEvidenceContentDescription: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val options: List<GSegmentOption<String?>> = GLevelOption.entries.map { option ->
        GSegmentOption<String?>(
            value = option.letter,
            label = option.letter,
            contentDescription = option.contentDescription,
        )
    } + GSegmentOption<String?>(
        value = noEvidenceValue,
        label = NO_LEVEL_LABEL,
        contentDescription = noEvidenceContentDescription,
        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        selectedBorderColor = MaterialTheme.colorScheme.outline,
    )

    GSegmentedPicker(
        options = options,
        selected = selected,
        onSelect = { tapped -> nonDeselectingTap(tapped, onSelect) },
        modifier = modifier,
        isEnabled = isEnabled,
        activeContentColor = MaterialTheme.colorScheme.onSurface,
        activeBorderColor = MaterialTheme.colorScheme.primary,
    )
}

internal fun nonDeselectingTap(tapped: String?, onSelect: (String) -> Unit) {
    if (tapped != null) {
        onSelect(tapped)
    }
}

@PreviewLightDark
@Composable
private fun GLevelPickerPreview() {
    GemaTheme {
        GLevelPicker(selected = GLevelOption.B.letter, onSelect = {})
    }
}
