package com.emm.gema.feature.export

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.core.domain.curriculum.CompetencyId
import com.emm.gema.core.domain.schoolyear.PeriodId
import com.emm.gema.core.domain.student.StudentId
import com.emm.gema.core.theme.GemaAccents
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.theme.label
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerActionStyle
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GDivider
import com.emm.gema.core.ui.GIcon
import com.emm.gema.core.ui.GListItem
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import java.time.YearMonth

@Composable
fun ExportScreen(
    state: ExportUiState,
    onIntent: (ExportUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    val scrollState: ScrollState = rememberScrollState()
    GScreen(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = {
            GTopBar(
                title = stringResource(R.string.export_title, state.sectionTitle),
                subtitle = state.periodLabel,
                onBackClick = { onIntent(ExportUiIntent.BackClicked) },
                isContentScrolled = scrollState.value > 0,
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .padding(vertical = GemaSpacing.screenGutter)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.large),
        ) {
            GradesGroup(state = state, onIntent = onIntent)
            AttendanceGroup(state = state, onIntent = onIntent)
            SummaryGroup(state = state, onIntent = onIntent)
            GText(
                text = stringResource(R.string.export_file_name_note),
                style = GTextStyle.BODY_SMALL,
            )
        }
    }
}

@Composable
private fun EyebrowLabel(text: String, modifier: Modifier = Modifier) {
    GText(
        text = text,
        style = GTextStyle.LABEL_SMALL,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
private fun GradesGroup(
    state: ExportUiState,
    onIntent: (ExportUiIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EyebrowLabel(text = stringResource(R.string.export_grades_title))
        TemplateMismatchBanner(
            mismatch = state.templateMismatch,
            modifier = Modifier.padding(top = GemaSpacing.small),
        )
        Column(
            modifier = Modifier.padding(top = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.rowGap),
        ) {
            when (val grades: GradesExportUiState = state.gradesExportState) {
                GradesExportUiState.Unavailable -> UnavailableGrades(onIntent = onIntent)
                GradesExportUiState.Ready -> ReadyGrades(state = state, onIntent = onIntent)
                is GradesExportUiState.Blocked -> BlockedGrades(state = state, gaps = grades.gaps, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun TemplateMismatchBanner(mismatch: TemplateMismatchUi?, modifier: Modifier = Modifier) {
    if (mismatch == null) return
    val missing: String = (mismatch.areaNames + mismatch.competencyLabels + mismatch.studentNames)
        .joinToString(separator = ", ")

    GBanner(
        text = stringResource(R.string.export_template_mismatch, missing),
        modifier = modifier,
        tone = GBannerTone.WARNING,
    )
}

@Composable
private fun UnavailableGrades(onIntent: (ExportUiIntent) -> Unit) {
    GBanner(
        text = stringResource(R.string.export_grades_unavailable),
        tone = GBannerTone.INFO,
        actionText = stringResource(R.string.export_import_template),
        onActionClick = { onIntent(ExportUiIntent.ImportTemplateClicked) },
        actionStyle = GBannerActionStyle.LINK,
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
        enabled = state.activeExport == null,
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
    GText(
        text = stringResource(R.string.export_grades_blocked, gaps.size),
        style = GTextStyle.BODY_LARGE_EMPHASIS,
        color = GemaAccents.onWarningContainer,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    Column {
        GDivider()
        gaps.forEach { gap: ExportGapRow ->
            GListItem(
                title = gap.studentName,
                subtitle = gap.competencyLabel,
                hasChevron = true,
                onClick = { onIntent(ExportUiIntent.GapRowClicked(gap)) },
            )
        }
    }
    GButton(
        text = stringResource(R.string.export_generate_file),
        onClick = { onIntent(ExportUiIntent.ExportGradesClicked) },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
    )
}

@Composable
private fun TemplateName(fileName: String?, modifier: Modifier = Modifier) {
    if (fileName == null) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GIcon(
            icon = Icons.Filled.Description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 18.dp,
        )
        GText(
            text = fileName,
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AttendanceGroup(state: ExportUiState, onIntent: (ExportUiIntent) -> Unit) {
    val month: YearMonth = state.attendanceMonth ?: return
    Column(modifier = Modifier.fillMaxWidth()) {
        GDivider()
        Column(
            modifier = Modifier.padding(top = GemaSpacing.eyebrowGap),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            EyebrowLabel(text = stringResource(R.string.export_attendance_title))
            GText(
                text = stringResource(
                    R.string.export_attendance_subtitle,
                    month.label(),
                    state.attendanceDayCount,
                ),
                style = GTextStyle.BODY_LARGE,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GButton(
                text = stringResource(R.string.export_attendance_export),
                onClick = { onIntent(ExportUiIntent.ExportAttendanceClicked) },
                modifier = Modifier.fillMaxWidth(),
                variant = GButtonVariant.SECONDARY,
                enabled = state.activeExport == null,
                isBusy = state.activeExport == ActiveExport.ATTENDANCE,
            )
        }
    }
}

internal fun summaryPdfVariant(gradesExportState: GradesExportUiState): GButtonVariant =
    if (gradesExportState == GradesExportUiState.Unavailable) GButtonVariant.PRIMARY else GButtonVariant.SECONDARY

@Composable
private fun SummaryGroup(state: ExportUiState, onIntent: (ExportUiIntent) -> Unit) {
    val pdfVariant: GButtonVariant = summaryPdfVariant(state.gradesExportState)
    Column(modifier = Modifier.fillMaxWidth()) {
        GDivider()
        Column(
            modifier = Modifier.padding(top = GemaSpacing.eyebrowGap),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
        ) {
            EyebrowLabel(text = stringResource(R.string.export_summary_title))
            GText(
                text = stringResource(R.string.export_summary_description),
                style = GTextStyle.BODY_LARGE,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GemaSpacing.small),
            ) {
                GButton(
                    text = stringResource(R.string.export_summary_pdf),
                    onClick = { onIntent(ExportUiIntent.ExportSummaryPdfClicked) },
                    modifier = Modifier.weight(1f),
                    variant = pdfVariant,
                    enabled = state.periodId != null && state.activeExport == null,
                    isBusy = state.activeExport == ActiveExport.SUMMARY_PDF,
                )
                GButton(
                    text = stringResource(R.string.export_summary_csv),
                    onClick = { onIntent(ExportUiIntent.ExportSummaryCsvClicked) },
                    modifier = Modifier.weight(1f),
                    variant = GButtonVariant.SECONDARY,
                    enabled = state.periodId != null && state.activeExport == null,
                    isBusy = state.activeExport == ActiveExport.SUMMARY_CSV,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ExportScreenPreview() {
    GemaTheme {
        ExportScreen(
            state = ExportUiState(
                isLoading = false,
                sectionTitle = "6to A",
                periodId = PeriodId("period-1"),
                periodLabel = "II Bimestre",
                templateFileName = "6 Primaria EBR.xlsx",
                attendanceMonth = YearMonth.of(2026, 9),
                attendanceDayCount = 20,
                gradesExportState = GradesExportUiState.Blocked(
                    listOf(
                        ExportGapRow(
                            studentId = StudentId("student-1"),
                            studentName = "BAUTISTA QUISPE, JOSE",
                            competencyId = CompetencyId("PPSS-2"),
                            competencyLabel = "Personal Social - 02",
                        ),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}
