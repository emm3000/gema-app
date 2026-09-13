package com.emm.gema.feature.sections.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.core.domain.section.label
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    GDivider()
                    GButton(
                        text = stringResource(R.string.sections_form_delete_button),
                        onClick = { onIntent(SectionFormUiIntent.DeleteClicked) },
                        variant = GButtonVariant.DESTRUCTIVE,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = GemaSpacing.medium),
                    )
                }
            }
        }
    }

    if (state.deleteConfirmation != null) {
        DeleteSectionDialog(
            confirmation = state.deleteConfirmation,
            onConfirm = { onIntent(SectionFormUiIntent.DeleteConfirmed) },
            onDismiss = { onIntent(SectionFormUiIntent.DeleteDismissed) },
        )
    }
}

@Composable
private fun DeleteSectionDialog(
    confirmation: DeleteConfirmation,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GDialog(
        title = stringResource(R.string.sections_form_delete_dialog_title),
        confirmText = stringResource(R.string.sections_form_delete_confirm),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        dismissText = stringResource(R.string.sections_form_delete_cancel),
        isDestructive = true,
    ) {
        GText(
            text = stringResource(
                R.string.sections_form_delete_message,
                confirmation.studentCount,
                confirmation.attendanceDayCount,
                confirmation.periodLevelCount,
            ),
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
