package com.emm.gema.feature.evaluation.levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.ui.GBottomSheet
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GCheckRow
import com.emm.gema.core.ui.GLevelPicker
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.feature.evaluation.R
import java.time.format.DateTimeFormatter

private val evidenceDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

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
            text = "Nivel de logro",
            style = GTextStyle.LABEL_SMALL,
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
            text = "O no evaluada",
            style = GTextStyle.LABEL_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall)) {
            UnworkedComment.entries.forEach { comment ->
                GCheckRow(
                    title = comment.label,
                    isChecked = sheet.unworkedComment == comment,
                    onCheckedChange = { isChecked ->
                        onIntent(
                            PeriodLevelsUiIntent.SheetUnworkedCommentSelected(comment.takeIf { isChecked }),
                        )
                    },
                )
            }
        }
        GTextField(
            value = sheet.descriptiveConclusion,
            onValueChange = { onIntent(PeriodLevelsUiIntent.SheetDescriptiveConclusionChanged(it)) },
            label = "Conclusión descriptiva",
            modifier = Modifier.fillMaxWidth(),
            supportingText = stringResource(R.string.period_levels_sheet_conclusion_hint)
                .takeIf { sheet.isConclusionRequiredForExport },
        )
        if (sheet.evidence.isNotEmpty()) {
            GText(
                text = "EVIDENCIAS DE ESTE PERIODO",
                style = GTextStyle.LABEL_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall)) {
                sheet.evidence.forEach { evidence -> EvidenceListRow(evidence) }
            }
        }
        GButton(
            text = "Listo",
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
            text = evidence.date.format(evidenceDateFormatter),
            style = GTextStyle.BODY_MEDIUM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GText(
            text = evidence.activityName,
            style = GTextStyle.BODY_MEDIUM,
            modifier = Modifier.weight(1f),
        )
        GText(
            text = evidence.achievementLevel.name,
            style = GTextStyle.BODY_MEDIUM,
        )
    }
}
