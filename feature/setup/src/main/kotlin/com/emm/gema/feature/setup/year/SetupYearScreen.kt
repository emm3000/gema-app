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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.kindLabel
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSegmentOption
import com.emm.gema.core.ui.GSegmentedPicker
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.setup.rangeLabel
import java.time.LocalDate

private const val REASSURANCE_TEXT: String =
    "Calculamos los periodos por ti. Toca uno para ajustar sus fechas, y puedes corregirlas después."

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
                GBanner(text = REASSURANCE_TEXT, modifier = Modifier.fillMaxWidth())
            }
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
            item {
                GText(
                    text = "Periodos",
                    style = GTextStyle.TITLE_MEDIUM,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(state.periods, key = { it.ordinal }) { row ->
                PeriodRow(row = row, onIntent = onIntent)
            }
        }
    }

    if (state.editor != null) {
        PeriodEditorDialog(editor = state.editor, state = state, onIntent = onIntent)
    }
}

@Composable
private fun PeriodRow(
    row: PeriodDraftRow,
    onIntent: (SetupYearUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GCard(modifier = modifier.fillMaxWidth()) {
        GListItem(
            title = row.label,
            subtitle = row.error ?: rangeLabel(row.startDate, row.endDate),
            hasChevron = true,
            onClick = { onIntent(SetupYearUiIntent.PeriodClicked(row.ordinal)) },
        )
    }
}

@Composable
private fun PeriodEditorDialog(
    editor: PeriodEditorState,
    state: SetupYearUiState,
    onIntent: (SetupYearUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    GDialog(
        title = editor.label,
        confirmText = "Guardar",
        onConfirm = { onIntent(SetupYearUiIntent.EditorConfirmed) },
        onDismiss = { onIntent(SetupYearUiIntent.EditorDismissed) },
        modifier = modifier,
        dismissText = "Cancelar",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
            GDateField(
                value = editor.startDate,
                onValueChange = { onIntent(SetupYearUiIntent.EditorStartDateChanged(it)) },
                label = "Empieza",
                modifier = Modifier.fillMaxWidth(),
                minDate = state.startDate,
                maxDate = state.endDate,
            )
            GDateField(
                value = editor.endDate,
                onValueChange = { onIntent(SetupYearUiIntent.EditorEndDateChanged(it)) },
                label = "Termina",
                modifier = Modifier.fillMaxWidth(),
                errorText = editor.error,
                minDate = state.startDate,
                maxDate = state.endDate,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SetupYearScreenPreview() {
    GemaTheme {
        SetupYearScreen(
            state = SetupYearUiState(
                yearLabel = "2026",
                startDate = LocalDate.of(2026, 3, 1),
                endDate = LocalDate.of(2026, 12, 20),
                periods = listOf(
                    PeriodDraftRow(1, "I Bimestre", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 13), null),
                    PeriodDraftRow(2, "II Bimestre", LocalDate.of(2026, 5, 14), LocalDate.of(2026, 7, 26), null),
                ),
                canContinue = true,
            ),
            onIntent = {},
        )
    }
}
