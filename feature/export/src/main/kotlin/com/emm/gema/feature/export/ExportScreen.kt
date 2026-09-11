package com.emm.gema.feature.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GCard
import com.emm.gema.core.ui.GDropdownPicker
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GPickerOption
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar

private const val CURRENT_PERIOD_BADGE: String = "Actual"

@Composable
fun ExportScreen(
    state: ExportUiState,
    onIntent: (ExportUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    GScreen(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = {
            GTopBar(
                title = stringResource(R.string.export_title),
                subtitle = state.sectionTitle,
                onBackClick = { onIntent(ExportUiIntent.BackClicked) },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .padding(GemaSpacing.screenGutter)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            GDropdownPicker(
                options = state.periods.map {
                    GPickerOption(
                        value = it.id,
                        label = it.label,
                        badge = CURRENT_PERIOD_BADGE.takeIf { _ -> it.isCurrent },
                    )
                },
                selected = state.selectedPeriodId,
                onSelect = { onIntent(ExportUiIntent.PeriodSelected(it)) },
                label = stringResource(R.string.export_period_label),
                modifier = Modifier.fillMaxWidth(),
            )
            GradesCard(state = state, onIntent = onIntent)
            SummaryCard(state = state, onIntent = onIntent)
            GText(
                text = stringResource(R.string.export_file_name_note),
                style = GTextStyle.BODY_SMALL,
            )
        }
    }
}

@Composable
private fun GradesCard(
    state: ExportUiState,
    onIntent: (ExportUiIntent) -> Unit,
) {
    GCard {
        GText(
            text = stringResource(R.string.export_grades_title),
            style = GTextStyle.TITLE_MEDIUM,
        )
        TemplateMismatchBanner(state.templateMismatch)
        when (val grades: GradesExportUiState = state.gradesExportState) {
            GradesExportUiState.Unavailable -> UnavailableGrades(onIntent = onIntent)
            GradesExportUiState.Ready -> ReadyGrades(state = state, onIntent = onIntent)
            is GradesExportUiState.Blocked -> BlockedGrades(state = state, gaps = grades.gaps, onIntent = onIntent)
        }
    }
}

@Composable
private fun TemplateMismatchBanner(mismatch: TemplateMismatchUi?) {
    if (mismatch == null) return
    val missing: String = (mismatch.areaNames + mismatch.competencyLabels + mismatch.studentNames)
        .joinToString(separator = ", ")

    GBanner(
        text = stringResource(R.string.export_template_mismatch, missing),
        tone = GBannerTone.WARNING,
    )
}

@Composable
private fun UnavailableGrades(onIntent: (ExportUiIntent) -> Unit) {
    GText(
        text = stringResource(R.string.export_grades_unavailable),
        style = GTextStyle.BODY_MEDIUM,
    )
    GButton(
        text = stringResource(R.string.export_import_template),
        onClick = { onIntent(ExportUiIntent.ImportTemplateClicked) },
        modifier = Modifier.fillMaxWidth(),
        variant = GButtonVariant.SECONDARY,
    )
}

@Composable
private fun ReadyGrades(
    state: ExportUiState,
    onIntent: (ExportUiIntent) -> Unit,
) {
    TemplateName(state.templateFileName)
    GButton(
        text = stringResource(R.string.export_generate_file),
        onClick = { onIntent(ExportUiIntent.ExportGradesClicked) },
        modifier = Modifier.fillMaxWidth(),
        isBusy = state.activeExport == ActiveExport.GRADES,
    )
}

@Composable
private fun BlockedGrades(
    state: ExportUiState,
    gaps: List<ExportGapRow>,
    onIntent: (ExportUiIntent) -> Unit,
) {
    TemplateName(state.templateFileName)
    GBanner(
        text = stringResource(R.string.export_grades_blocked, gaps.size),
        tone = GBannerTone.WARNING,
    )
    gaps.forEach { gap: ExportGapRow ->
        GListItem(
            title = gap.studentName,
            subtitle = gap.competencyLabel,
            hasChevron = true,
            onClick = { onIntent(ExportUiIntent.GapRowClicked(gap)) },
        )
    }
    GButton(
        text = stringResource(R.string.export_generate_file),
        onClick = { onIntent(ExportUiIntent.ExportGradesClicked) },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
    )
}

@Composable
private fun SummaryCard(state: ExportUiState, onIntent: (ExportUiIntent) -> Unit) {
    GCard {
        GText(
            text = stringResource(R.string.export_summary_title),
            style = GTextStyle.TITLE_MEDIUM,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            GButton(
                text = stringResource(R.string.export_summary_pdf),
                onClick = { onIntent(ExportUiIntent.ExportSummaryPdfClicked) },
                modifier = Modifier.weight(1f),
                variant = GButtonVariant.SECONDARY,
                enabled = state.selectedPeriodId != null && state.activeExport != ActiveExport.SUMMARY_CSV,
                isBusy = state.activeExport == ActiveExport.SUMMARY_PDF,
            )
            GButton(
                text = stringResource(R.string.export_summary_csv),
                onClick = { onIntent(ExportUiIntent.ExportSummaryCsvClicked) },
                modifier = Modifier.weight(1f),
                variant = GButtonVariant.SECONDARY,
                enabled = state.selectedPeriodId != null && state.activeExport != ActiveExport.SUMMARY_PDF,
                isBusy = state.activeExport == ActiveExport.SUMMARY_CSV,
            )
        }
    }
}

@Composable
private fun TemplateName(fileName: String?) {
    if (fileName != null) {
        GText(text = fileName, style = GTextStyle.BODY_MEDIUM)
    }
}

@PreviewLightDark
@Composable
private fun ExportScreenPreview() {
    GemaTheme {
        ExportScreen(
            state = ExportUiState(
                isLoading = false,
                sectionTitle = "6° A",
                periods = listOf(PeriodOption(id = "period-1", label = "II Bimestre", isCurrent = true)),
                selectedPeriodId = "period-1",
                templateFileName = "6 Primaria EBR.xlsx",
                gradesExportState = GradesExportUiState.Blocked(
                    listOf(
                        ExportGapRow(
                            studentId = "student-1",
                            studentName = "BAUTISTA QUISPE, JOSE",
                            competencyId = "PPSS-2",
                            competencyLabel = "Personal Social - 02",
                        ),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
