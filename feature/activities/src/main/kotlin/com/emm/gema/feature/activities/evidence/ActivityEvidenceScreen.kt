package com.emm.gema.feature.activities.evidence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.evaluation.AchievementLevel
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GDropdownPicker
import com.emm.gema.core.ui.GLevelPicker
import com.emm.gema.core.ui.GPickerOption
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar

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
                subtitle = "${state.activityDateLabel} - ${state.periodLabel}",
                onBackClick = { onIntent(ActivityEvidenceUiIntent.BackClicked) },
                actions = {
                    GButton(
                        text = "Editar",
                        onClick = { onIntent(ActivityEvidenceUiIntent.EditActivityClicked) },
                        variant = GButtonVariant.TEXT,
                    )
                },
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        Column(modifier = Modifier.padding(padding)) {
            if (message != null) {
                GBanner(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = GemaSpacing.small),
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
                    StudentRow(row = row, onSelect = { level ->
                        onIntent(ActivityEvidenceUiIntent.LevelSelected(row.studentId, level))
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
            .padding(vertical = GemaSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GDropdownPicker(
            options = state.competencies.map { GPickerOption(value = it.id, label = it.label) },
            selected = state.selectedCompetencyId,
            onSelect = { onIntent(ActivityEvidenceUiIntent.CompetencySelected(it)) },
            label = "Competencia",
            modifier = Modifier.fillMaxWidth(),
        )
        GText(
            text = "${state.recordedCount}/${state.totalCount}",
            style = GTextStyle.BODY_MEDIUM,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StudentRow(row: EvidenceLevelRow, onSelect: (AchievementLevel?) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GemaSpacing.small),
    ) {
        GText(text = row.displayName, style = GTextStyle.BODY_LARGE)
        GLevelPicker(
            selected = row.level?.name,
            onSelect = { letter -> onSelect(letter?.let(AchievementLevel::valueOf)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
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
                recordedCount = 1,
                totalCount = 2,
                rows = listOf(
                    EvidenceLevelRow(StudentId("student-1"), "ACOSTA RIVERA, Luz Maria", AchievementLevel.B),
                    EvidenceLevelRow(StudentId("student-2"), "BAUTISTA QUISPE, Jose", null),
                ),
            ),
            onIntent = {},
        )
    }
}
