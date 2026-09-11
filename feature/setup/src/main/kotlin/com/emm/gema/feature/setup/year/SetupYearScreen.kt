package com.emm.gema.feature.setup.year

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSegmentOption
import com.emm.gema.core.ui.GSegmentedPicker
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.setup.kindLabel
import java.time.LocalDate

@Composable
fun SetupYearScreen(
    state: SetupYearUiState,
    onIntent: (SetupYearUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Año escolar",
                subtitle = "Paso 1 de 2",
                onBackClick = { onIntent(SetupYearUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Continuar",
                onClick = { onIntent(SetupYearUiIntent.ContinueClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canContinue,
            )
        },
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            item {
                GTextField(
                    value = state.yearLabel,
                    onValueChange = { onIntent(SetupYearUiIntent.YearLabelChanged(it)) },
                    label = "Nombre del año escolar",
                    modifier = Modifier.fillMaxWidth(),
                    errorText = state.yearLabelError,
                )
            }
            item {
                GDateField(
                    value = state.startDate,
                    onValueChange = { onIntent(SetupYearUiIntent.StartDateChanged(it)) },
                    label = "Empieza",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                GDateField(
                    value = state.endDate,
                    onValueChange = { onIntent(SetupYearUiIntent.EndDateChanged(it)) },
                    label = "Termina",
                    modifier = Modifier.fillMaxWidth(),
                    errorText = state.dateRangeError,
                )
            }
            item {
                GSegmentedPicker(
                    options = PeriodKind.entries.map {
                        GSegmentOption(value = it, label = it.kindLabel(), contentDescription = it.kindLabel())
                    },
                    selected = state.periodKind,
                    onSelect = { kind -> kind?.let { onIntent(SetupYearUiIntent.PeriodKindSelected(it)) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            items(state.periods, key = { it.ordinal }) { row ->
                PeriodDraftEditor(row = row, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun PeriodDraftEditor(
    row: PeriodDraftRow,
    onIntent: (SetupYearUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        GDateField(
            value = row.startDate,
            onValueChange = { onIntent(SetupYearUiIntent.PeriodStartDateChanged(row.ordinal, it)) },
            label = "Empieza",
            modifier = Modifier.fillMaxWidth(),
        )
        GDateField(
            value = row.endDate,
            onValueChange = { onIntent(SetupYearUiIntent.PeriodEndDateChanged(row.ordinal, it)) },
            label = "Termina",
            modifier = Modifier.fillMaxWidth(),
            errorText = row.error,
        )
    }
}

@PreviewLightDark
@Composable
private fun SetupYearScreenPreview() {
    GemaTheme {
        SetupYearScreen(
            state = SetupYearUiState(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 2),
                endDate = LocalDate.of(2026, 12, 18),
                periods = listOf(
                    PeriodDraftRow(1, "I Bimestre", LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 13), null),
                ),
                canContinue = true,
            ),
            onIntent = {},
        )
    }
}
