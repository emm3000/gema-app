package com.emm.gema.feature.evaluation.levels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emm.gema.core.domain.evaluation.UnworkedComment
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.ui.GBottomSheet
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GCheckRow
import com.emm.gema.core.ui.GLevelPicker
import com.emm.gema.core.ui.GTextField

private const val CONCLUSION_HINT: String = "Obligatoria para SIAGIE cuando el nivel es C. Puedes guardarla después."

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
        Text(
            text = "Nivel de logro",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GLevelPicker(
            selected = sheet.achievementLevel?.toOption(),
            onSelect = { onIntent(PeriodLevelsUiIntent.SheetAchievementLevelSelected(it?.toAchievementLevel())) },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "O no evaluada",
            style = MaterialTheme.typography.labelSmall,
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
            supportingText = CONCLUSION_HINT.takeIf { sheet.isConclusionRequiredForExport },
        )
        GButton(
            text = "Listo",
            onClick = { onIntent(PeriodLevelsUiIntent.SheetDismissed) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
