package com.emm.gema.feature.sections.form

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.label
import com.emm.gema.core.domain.section.sectionTitle
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GChoiceChipOption
import com.emm.gema.core.ui.GChoiceChipRow
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GDivider
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.ui.toTextStyle
import com.emm.gema.feature.sections.R

@Composable
private fun gradeOptions(): List<GChoiceChipOption<Grade>> = Grade.entries.map { grade ->
    GChoiceChipOption(
        value = grade,
        label = "${grade.number}",
        contentDescription = stringResource(R.string.sections_form_grade_content_description, grade.label()),
    )
}

@Composable
private fun sectionFormSubtitle(state: SectionFormUiState): String? {
    val grade: Grade = state.grade ?: return null
    if (state.sectionId == null) return null
    return stringResource(
        R.string.sections_form_subtitle,
        grade.label(),
        state.sectionName,
        pluralStringResource(R.plurals.sections_form_student_count, state.studentCount, state.studentCount),
    )
}

private fun sectionFormTitle(state: SectionFormUiState): String {
    val grade: Grade = state.grade ?: return state.sectionName
    return sectionTitle(grade, state.sectionName)
}

@Composable
fun SectionFormScreen(
    state: SectionFormUiState,
    onIntent: (SectionFormUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = if (state.sectionId == null) {
                    stringResource(R.string.sections_form_title_new)
                } else {
                    stringResource(R.string.sections_form_title_edit)
                },
                subtitle = sectionFormSubtitle(state),
                onBackClick = { onIntent(SectionFormUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = stringResource(R.string.sections_form_save),
                onClick = { onIntent(SectionFormUiIntent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSave,
            )
        },
    ) { padding: PaddingValues ->
        val scrollState: ScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(top = GemaSpacing.small),
            verticalArrangement = Arrangement.spacedBy(GemaSpacing.medium),
        ) {
            if (message != null) {
                GBanner(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    tone = GBannerTone.ERROR,
                    actionText = stringResource(R.string.sections_form_understood),
                    onActionClick = onMessageDismissed,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
                GText(
                    text = stringResource(R.string.sections_form_grade_label),
                    style = GTextStyle.LABEL_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GChoiceChipRow(
                    options = gradeOptions(),
                    selected = state.grade,
                    onSelect = { grade -> grade?.let { onIntent(SectionFormUiIntent.GradeSelected(it)) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GTextField(
                value = state.sectionName,
                onValueChange = { onIntent(SectionFormUiIntent.SectionNameChanged(it)) },
                label = stringResource(R.string.sections_form_name_label),
                modifier = Modifier.fillMaxWidth(),
                errorText = state.sectionNameError?.let { stringResource(sectionFormMessageRes(it)) },
            )
            if (state.canDelete) {
                DangerZone(onIntent = onIntent)
            }
        }
    }

    if (state.deleteConfirmation != null) {
        DeleteSectionDialog(
            sectionTitle = sectionFormTitle(state),
            confirmation = state.deleteConfirmation,
            onConfirm = { onIntent(SectionFormUiIntent.DeleteConfirmed) },
            onDismiss = { onIntent(SectionFormUiIntent.DeleteDismissed) },
        )
    }
}

@Composable
private fun DangerZone(onIntent: (SectionFormUiIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = GemaSpacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(GemaSpacing.small),
    ) {
        GText(
            text = stringResource(R.string.sections_form_delete_eyebrow),
            style = GTextStyle.LABEL_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GButton(
            text = stringResource(R.string.sections_form_delete_button),
            onClick = { onIntent(SectionFormUiIntent.DeleteClicked) },
            variant = GButtonVariant.DESTRUCTIVE,
            modifier = Modifier.fillMaxWidth(),
        )
        GText(
            text = stringResource(R.string.sections_form_delete_helper),
            style = GTextStyle.BODY_SMALL,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeleteSectionDialog(
    sectionTitle: String,
    confirmation: DeleteConfirmation,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GDialog(
        title = stringResource(R.string.sections_form_delete_dialog_title, sectionTitle),
        confirmText = stringResource(R.string.sections_form_delete_confirm),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        dismissText = stringResource(R.string.sections_form_delete_cancel),
        isDestructive = true,
    ) {
        GText(
            text = stringResource(R.string.sections_form_delete_message),
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DeleteCounts(confirmation = confirmation, modifier = Modifier.padding(top = GemaSpacing.small))
    }
}

private data class DeleteCountRowSpec(val count: Int, val label: String, val rowDescription: String)

@Composable
private fun deleteCountRowSpecs(confirmation: DeleteConfirmation): List<DeleteCountRowSpec> = listOf(
    DeleteCountRowSpec(
        count = confirmation.studentCount,
        label = pluralStringResource(R.plurals.sections_form_delete_row_students, confirmation.studentCount),
        rowDescription = pluralStringResource(
            R.plurals.sections_form_delete_student_count,
            confirmation.studentCount,
            confirmation.studentCount,
        ),
    ),
    DeleteCountRowSpec(
        count = confirmation.attendanceDayCount,
        label = pluralStringResource(
            R.plurals.sections_form_delete_row_attendance_days,
            confirmation.attendanceDayCount,
        ),
        rowDescription = pluralStringResource(
            R.plurals.sections_form_delete_attendance_days_count,
            confirmation.attendanceDayCount,
            confirmation.attendanceDayCount,
        ),
    ),
    DeleteCountRowSpec(
        count = confirmation.periodLevelCount,
        label = pluralStringResource(
            R.plurals.sections_form_delete_row_period_levels,
            confirmation.periodLevelCount,
        ),
        rowDescription = pluralStringResource(
            R.plurals.sections_form_delete_period_levels_count,
            confirmation.periodLevelCount,
            confirmation.periodLevelCount,
        ),
    ),
)

@Composable
private fun widestCountWidth(specs: List<DeleteCountRowSpec>): Dp {
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val numeralStyle: TextStyle = GTextStyle.NUMERAL.toTextStyle()
    val density: Density = LocalDensity.current
    val widestCountPx: Int = specs.maxOf { spec: DeleteCountRowSpec ->
        textMeasurer.measure(text = spec.count.toString(), style = numeralStyle).size.width
    }
    val widestCountWidth: Dp = with(density) { widestCountPx.toDp() }
    return widestCountWidth.coerceAtLeast(GemaSpacing.narrowCellWidth)
}

@Composable
private fun DeleteCounts(confirmation: DeleteConfirmation, modifier: Modifier = Modifier) {
    val specs: List<DeleteCountRowSpec> = deleteCountRowSpecs(confirmation)
    val countWidth: Dp = widestCountWidth(specs)
    Column(modifier = modifier.fillMaxWidth()) {
        GDivider()
        specs.forEach { spec: DeleteCountRowSpec ->
            DeleteCountRow(spec = spec, countWidth = countWidth)
            GDivider()
        }
    }
}

@Composable
private fun DeleteCountRow(spec: DeleteCountRowSpec, countWidth: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = GemaSpacing.compactLineHeight)
            .semantics(mergeDescendants = true) { contentDescription = spec.rowDescription },
        horizontalArrangement = Arrangement.spacedBy(GemaSpacing.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(countWidth), contentAlignment = Alignment.CenterEnd) {
            GText(text = spec.count.toString(), style = GTextStyle.NUMERAL)
        }
        GText(
            text = spec.label,
            style = GTextStyle.BODY_LARGE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun SectionFormScreenPreview() {
    GemaTheme {
        SectionFormScreen(
            state = SectionFormUiState(
                isLoading = false,
                sectionId = SectionId("section-1"),
                grade = Grade.THIRD,
                sectionName = "A",
                canSave = true,
                canDelete = true,
                studentCount = 30,
            ),
            onIntent = {},
        )
    }
}
