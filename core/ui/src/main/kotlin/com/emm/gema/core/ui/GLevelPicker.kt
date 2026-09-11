package com.emm.gema.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.theme.GemaTheme

private const val NO_LEVEL_LABEL: String = "—"
private const val NO_LEVEL_DESCRIPTION: String = "Sin nivel"

@Composable
fun GLevelPicker(
    selected: AchievementLevel?,
    onSelect: (AchievementLevel?) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val options: List<GSegmentOption<AchievementLevel?>> =
        AchievementLevel.entries.map { level ->
            GSegmentOption<AchievementLevel?>(
                value = level,
                label = level.name,
                contentDescription = level.officialName,
            )
        } + GSegmentOption<AchievementLevel?>(
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
    )
}

@PreviewLightDark
@Composable
private fun GLevelPickerPreview() {
    GemaTheme {
        GLevelPicker(selected = AchievementLevel.B, onSelect = {})
    }
}
