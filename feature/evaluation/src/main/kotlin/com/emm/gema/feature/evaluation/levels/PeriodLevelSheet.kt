package com.emm.gema.feature.evaluation.levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.asDayMonth
import com.emm.gema.core.ui.GBottomSheet
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GLevelChip
import com.emm.gema.core.ui.GLevelChipSize
import com.emm.gema.core.ui.GLevelPicker
import com.emm.gema.core.ui.GRadioRow
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.feature.evaluation.R

@Composable
fun PeriodLevelSheet(
    sheet: PeriodLevelSheetUiState,
    onIntent: (PeriodLevelsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GBottomSheet(
        onDismissRequest = { onIntent(PeriodLevelsUiIntent.SheetDismissed) },
        modifier = modifier,
        title = sheet.studentName,
        subtitle = sheet.competencyLabel,
    ) {
        GText(
            text = stringResource(R.string.period_levels_sheet_label_achievement_level),
            style = GTextStyle.LABEL_SMALL_EMPHASIS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GLevelPicker(
            selected = sheet.achievementLevel?.name,
            onSelect = {
                onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(it?.let(AchievementLevel::valueOf)))
            },
            modifier = Modifier.fillMaxWidth(),
        )
        GText(
            text = stringResource(R.string.period_levels_sheet_label_unworked),
            style = GTextStyle.LABEL_SMALL_EMPHASIS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall),
        ) {
            UnworkedComment.entries.forEach { comment ->
                GRadioRow(
                    title = comment.label,
                    isSelected = sheet.unworkedComment == comment,
                    onClick = {
                        val nextComment: UnworkedComment? = comment.takeIf { sheet.unworkedComment != comment }
                        onIntent(PeriodLevelsUiIntent.SheetUnworkedCommentSelected(nextComment))
                    },
                )
            }
        }
        GTextField(
            value = sheet.descriptiveConclusion,
            onValueChange = { onIntent(PeriodLevelsUiIntent.SheetDescriptiveConclusionChanged(it)) },
            label = stringResource(R.string.period_levels_sheet_label_conclusion),
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(R.string.period_levels_sheet_conclusion_hint),
        )
        if (sheet.evidence.isNotEmpty()) {
            GText(
                text = stringResource(R.string.period_levels_sheet_label_evidence),
                style = GTextStyle.LABEL_SMALL_EMPHASIS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall)) {
                sheet.evidence.forEach { evidence -> EvidenceListRow(evidence) }
            }
        }
        GButton(
            text = stringResource(R.string.period_levels_sheet_button_done),
            onClick = { onIntent(PeriodLevelsUiIntent.SheetDismissed) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EvidenceListRow(evidence: EvidenceRow) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GText(
            text = evidence.date.asDayMonth(),
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GText(
            text = evidence.activityName,
            style = GTextStyle.BODY_LARGE,
            modifier = Modifier.weight(1f),
        )
        GLevelChip(
            letter = evidence.achievementLevel.name,
            size = GLevelChipSize.EVIDENCE,
        )
    }
}
