package com.emm.gema.feature.evaluation.levels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.emm.gema.feature.evaluation.R

@Composable
fun ColumnModeBar(
    heading: String,
    studentName: String,
    achievementLevel: AchievementLevel?,
    onIntent: (PeriodLevelsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface, shape = GemaShapes.container)
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
                    style = GTextStyle.LABEL_SMALL_EMPHASIS,
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
                text = stringResource(R.string.period_levels_column_mode_done),
                onClick = { onIntent(PeriodLevelsUiIntent.ExitColumnMode) },
                variant = GButtonVariant.TEXT,
            )
        }
        GLevelPicker(
            selected = achievementLevel?.name,
            onSelect = { onIntent(PeriodLevelsUiIntent.PickLevelForCurrent(it?.let(AchievementLevel::valueOf))) },
            modifier = Modifier.fillMaxWidth(),
        )
        GText(
            text = stringResource(R.string.period_levels_column_mode_caption),
            style = GTextStyle.BODY_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun ColumnModeBarPreview() {
    GemaTheme {
        ColumnModeBar(
            heading = "COMPETENCIA 01 · 4 DE 30",
            studentName = "DELGADO HUAMÁN, Pedro",
            achievementLevel = null,
            onIntent = {},
        )
    }
}
