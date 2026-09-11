package com.emm.gema.feature.activities.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.activity.ActivityId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GDropdownPicker
import com.emm.gema.core.ui.GEmptyState
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GPickerOption
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.activities.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

@Composable
fun ActivitiesScreen(
    state: ActivitiesUiState,
    onIntent: (ActivitiesUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentPeriodBadge: String = stringResource(R.string.activities_list_badge_current)
    GScreen(
        topBar = {
            GTopBar(
                title = "Actividades",
                subtitle = state.sectionTitle,
                onBackClick = { onIntent(ActivitiesUiIntent.BackClicked) },
                actions = {
                    GButton(
                        text = "Nueva actividad",
                        onClick = { onIntent(ActivitiesUiIntent.AddActivityClicked) },
                        variant = GButtonVariant.TEXT,
                    )
                },
            )
        },
        modifier = modifier,
    ) { padding: PaddingValues ->
        Column(modifier = Modifier.padding(padding)) {
            GDropdownPicker(
                options = state.periods.map {
                    GPickerOption(
                        value = it.id,
                        label = it.label,
                        badge = currentPeriodBadge.takeIf { _ -> it.isCurrent },
                    )
                },
                selected = state.selectedPeriodId,
                onSelect = { onIntent(ActivitiesUiIntent.PeriodSelected(it)) },
                label = "Periodo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = GemaSpacing.small),
            )
            if (state.activities.isEmpty()) {
                GEmptyState(
                    title = "Todavía no hay actividades",
                    message = "Registra una actividad para este periodo y enlázala a las competencias trabajadas.",
                    actionLabel = "Nueva actividad",
                    onActionClick = { onIntent(ActivitiesUiIntent.AddActivityClicked) },
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = GemaSpacing.small)) {
                    items(state.activities, key = { it.id.value }) { row ->
                        GListItem(
                            title = row.name,
                            subtitle = row.subtitle(),
                            trailingText = row.date.format(dateFormatter),
                            hasChevron = true,
                            onClick = { onIntent(ActivitiesUiIntent.ActivityClicked(row.id)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

private fun ActivityRow.subtitle(): String {
    val competencies: String = competencyLabels.joinToString(", ")
    return "$competencies - $evidenceRecordedCount de $studentCount con evidencia"
}

@PreviewLightDark
@Composable
private fun ActivitiesScreenPreview() {
    GemaTheme {
        ActivitiesScreen(
            state = ActivitiesUiState(
                isLoading = false,
                sectionTitle = "3ro A",
                periods = listOf(PeriodOption(PeriodId("period-2"), "II Bimestre", isCurrent = true)),
                selectedPeriodId = PeriodId("period-2"),
                activities = listOf(
                    ActivityRow(
                        id = ActivityId("activity-1"),
                        name = "Debate del aula",
                        date = LocalDate.of(2026, 6, 22),
                        competencyLabels = listOf("PPSS 01", "PPSS 02"),
                        evidenceRecordedCount = 30,
                        studentCount = 30,
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
