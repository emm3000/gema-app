package com.emm.gema.feature.evaluation.levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GLevelPicker
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle

@Composable
fun ColumnModeBar(
    heading: String,
    studentName: String,
    achievementLevel: AchievementLevel?,
    onIntent: (PeriodLevelsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GemaShapes.container,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    GText(
                        text = heading,
                        style = GTextStyle.LABEL_SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    GText(
                        text = studentName,
                        style = GTextStyle.BODY_LARGE,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                GButton(
                    text = "Listo",
                    onClick = { onIntent(PeriodLevelsUiIntent.ExitColumnMode) },
                    variant = GButtonVariant.TEXT,
                )
            }
            GLevelPicker(
                selected = achievementLevel?.name,
                onSelect = { onIntent(PeriodLevelsUiIntent.PickLevelForCurrent(it?.let(AchievementLevel::valueOf))) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ColumnModeBarPreview() {
    GemaTheme {
        ColumnModeBar(
            heading = "01 Construye su identidad - 4 de 30",
            studentName = "DELGADO HUAMÁN, Pedro",
            achievementLevel = null,
            onIntent = {},
        )
    }
}
