package com.emm.gema.feature.setup.year

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.schoolyear.PeriodKind
import com.emm.gema.core.domain.schoolyear.kindLabel
import com.emm.gema.feature.setup.R
import com.emm.gema.feature.setup.resolve
import com.emm.gema.core.theme.GemaBorder
import com.emm.gema.core.theme.GemaShapes
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GDateField
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSegmentOption
import com.emm.gema.core.ui.GSegmentedPicker
import com.emm.gema.core.ui.GStepHeader
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.setup.shortRangeLabel
import java.time.LocalDate

@Composable
fun SetupYearScreen(
    state: SetupYearUiState,
    onIntent: (SetupYearUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState: LazyListState = rememberLazyListState()
    GScreen(
        topBar = {
            GTopBar(
                title = "",
                onBackClick = { onIntent(SetupYearUiIntent.BackClicked) },
                isContentScrolled = listState.canScrollBackward,
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
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = GemaSpacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            item {
                GStepHeader(
                    step = stringResource(R.string.setup_year_step_label),
                    title = stringResource(R.string.setup_year_title),
                    description = stringResource(R.string.setup_year_helper_text),
                )
            }
            item {
                GTextField(
                    value = state.yearLabel,
                    onValueChange = { onIntent(SetupYearUiIntent.YearLabelChanged(it)) },
                    label = "Año",
                    modifier = Modifier.fillMaxWidth(),
                    errorText = state.yearLabelError?.resolve(),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
                ) {
                    GDateField(
                        value = state.startDate,
                        onValueChange = { onIntent(SetupYearUiIntent.StartDateChanged(it)) },
                        label = "Inicio",
                        modifier = Modifier.weight(1f),
                    )
                    GDateField(
                        value = state.endDate,
                        onValueChange = { onIntent(SetupYearUiIntent.EndDateChanged(it)) },
                        label = "Fin",
                        modifier = Modifier.weight(1f),
                        errorText = state.dateRangeError?.resolve(),
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
                    GText(
                        text = "¿Cómo evalúa tu escuela?",
                        style = GTextStyle.LABEL_SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GSegmentedPicker(
                        options = PeriodKind.entries.map {
                            GSegmentOption(
                                value = it,
                                label = "${it.kindLabel()}s (${it.periodCount})",
                                contentDescription = it.kindLabel(),
                            )
                        },
                        selected = state.periodKind,
                        onSelect = { kind -> kind?.let { onIntent(SetupYearUiIntent.PeriodKindSelected(it)) } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
                    GText(
                        text = "Periodos",
                        style = GTextStyle.LABEL_SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PeriodsList(periods = state.periods, onIntent = onIntent)
                    GText(
                        text = stringResource(R.string.setup_year_periods_caption),
                        style = GTextStyle.BODY_SMALL,
                    )
                }
            }
        }
    }

    if (state.editor != null) {
        PeriodEditorDialog(editor = state.editor, state = state, onIntent = onIntent)
    }
}

@Composable
private fun PeriodsList(
    periods: List<PeriodDraftRow>,
    onIntent: (SetupYearUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(GemaShapes.container)
            .border(GemaBorder.hairline, MaterialTheme.colorScheme.outline, GemaShapes.container),
    ) {
        periods.forEachIndexed { index, row ->
            GListItem(
                title = row.error?.resolve() ?: shortRangeLabel(row.startDate, row.endDate),
                leadingText = row.ordinal.toRomanNumeral(),
                showDivider = index < periods.lastIndex,
                onClick = { onIntent(SetupYearUiIntent.PeriodClicked(row.ordinal)) },
            )
        }
    }
}

private fun Int.toRomanNumeral(): String = listOf("I", "II", "III", "IV")[this - 1]

@Composable
private fun SetupYearMessage.resolve(): String = when (this) {
    SetupYearMessage.MISSING_LABEL -> stringResource(R.string.setup_year_error_missing_label)
    SetupYearMessage.INVALID_RANGE -> stringResource(R.string.setup_year_error_invalid_range)
    SetupYearMessage.TOO_SHORT -> stringResource(R.string.setup_year_error_too_short)
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            GDateField(
                value = editor.startDate,
                onValueChange = { onIntent(SetupYearUiIntent.EditorStartDateChanged(it)) },
                label = "Inicio",
                modifier = Modifier.weight(1f),
                minDate = state.startDate,
                maxDate = state.endDate,
            )
            GDateField(
                value = editor.endDate,
                onValueChange = { onIntent(SetupYearUiIntent.EditorEndDateChanged(it)) },
                label = "Fin",
                modifier = Modifier.weight(1f),
                errorText = editor.error?.resolve(),
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
