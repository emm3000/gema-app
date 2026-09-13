package com.emm.gema.feature.activities.evidence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.activity.EvidenceMark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaAccents
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GDropdownPicker
import com.emm.gema.core.ui.GIconButton
import com.emm.gema.core.ui.GLevelPicker
import com.emm.gema.core.ui.GPickerOption
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.activities.R

private const val NO_EVIDENCE_PICKER_VALUE: String = "NO_EVIDENCE"

@Composable
fun ActivityEvidenceScreen(
    state: ActivityEvidenceUiState,
    onIntent: (ActivityEvidenceUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = state.activityName,
                subtitle = stringResource(
                    R.string.activity_evidence_subtitle,
                    state.activityDateLabel,
                    state.periodLabel,
                ),
                onBackClick = { onIntent(ActivityEvidenceUiIntent.BackClicked) },
                actions = {
                    GIconButton(
                        icon = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.activity_evidence_edit_content_description),
                        onClick = { onIntent(ActivityEvidenceUiIntent.EditActivityClicked) },
                    )
                },
            )
        },
        modifier = modifier,
        contentGutter = false,
    ) { padding: PaddingValues ->
        Column(modifier = Modifier.padding(padding)) {
            if (message != null) {
                GBanner(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.small),
                    tone = GBannerTone.ERROR,
                    actionText = "Entendido",
                    onActionClick = onMessageDismissed,
                )
            }
            CompetencySelector(state = state, onIntent = onIntent)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = GemaSpacing.small),
            ) {
                items(state.rows, key = { it.studentId.value }) { row ->
                    StudentRow(row = row, onSelect = { mark ->
                        onIntent(ActivityEvidenceUiIntent.LevelSelected(row.studentId, mark))
                    })
                }
            }
        }
    }
}

@Composable
private fun CompetencySelector(state: ActivityEvidenceUiState, onIntent: (ActivityEvidenceUiIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GDropdownPicker(
            options = state.competencies.map { GPickerOption(value = it.id, label = it.label) },
            selected = state.selectedCompetencyId,
            onSelect = { onIntent(ActivityEvidenceUiIntent.CompetencySelected(it)) },
            label = "Competencia",
            modifier = Modifier.weight(1f),
        )
        GText(
            text = "${state.recordedCount}/${state.totalCount}",
            style = GTextStyle.LABEL_LARGE_EMPHASIS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StudentRow(row: EvidenceLevelRow, onSelect: (EvidenceMark?) -> Unit) {
    val isUntouched: Boolean = row.mark == null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUntouched) GemaAccents.warningContainer else MaterialTheme.colorScheme.surface)
            .padding(horizontal = GemaSpacing.screenGutter, vertical = GemaSpacing.small),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GText(text = row.displayName, style = GTextStyle.BODY_LARGE)
            if (isUntouched) {
                GText(
                    text = stringResource(R.string.activity_evidence_no_evidence_label),
                    style = GTextStyle.LABEL_SMALL,
                    color = GemaAccents.onWarningContainer,
                )
            }
        }
        val noEvidenceContentDescription: String =
            stringResource(R.string.activity_evidence_no_evidence_content_description)
        GLevelPicker(
            selected = row.mark.toPickerValue(),
            onSelect = { value -> onSelect(value.toEvidenceMark()) },
            noEvidenceValue = NO_EVIDENCE_PICKER_VALUE,
            noEvidenceContentDescription = noEvidenceContentDescription,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun EvidenceMark?.toPickerValue(): String? = when (this) {
    null -> null
    EvidenceMark.NoEvidence -> NO_EVIDENCE_PICKER_VALUE
    is EvidenceMark.Level -> achievementLevel.name
}

private fun String.toEvidenceMark(): EvidenceMark = when (this) {
    NO_EVIDENCE_PICKER_VALUE -> EvidenceMark.NoEvidence
    else -> EvidenceMark.Level(AchievementLevel.valueOf(this))
}

@PreviewLightDark
@Composable
private fun ActivityEvidenceScreenPreview() {
    GemaTheme {
        ActivityEvidenceScreen(
            state = ActivityEvidenceUiState(
                isLoading = false,
                activityName = "Debate del aula",
                activityDateLabel = "22/06/2026",
                periodLabel = "II Bimestre",
                competencies = listOf(CompetencyColumn(CompetencyId("PPSS-1"), "PPSS 01")),
                selectedCompetencyId = CompetencyId("PPSS-1"),
                recordedCount = 2,
                totalCount = 3,
                rows = listOf(
                    EvidenceLevelRow(
                        StudentId("student-1"),
                        "ACOSTA RIVERA, Luz Maria",
                        EvidenceMark.Level(AchievementLevel.B),
                    ),
                    EvidenceLevelRow(StudentId("student-2"), "BAUTISTA QUISPE, Jose", EvidenceMark.NoEvidence),
                    EvidenceLevelRow(StudentId("student-3"), "CCAHUANA MAMANI, Rosa", null),
                ),
            ),
            onIntent = {},
        )
    }
}
