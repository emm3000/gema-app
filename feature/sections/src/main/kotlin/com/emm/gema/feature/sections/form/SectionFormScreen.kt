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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GButtonVariant
import com.emm.gema.core.ui.GDialog
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSegmentOption
import com.emm.gema.core.ui.GSegmentedPicker
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.domain.section.label
import com.emm.gema.feature.sections.R

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
                .padding(GemaSpacing.screenGutter),
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
            GSegmentedPicker(
                options = buildList {
                    for (grade in Grade.entries) {
                        add(
                            GSegmentOption(
                                value = grade,
                                label = grade.label(),
                                contentDescription = stringResource(
                                    R.string.sections_form_grade_content_description,
                                    grade.label(),
                                ),
                            ),
                        )
                    }
                },
                selected = state.grade,
                onSelect = { grade -> grade?.let { onIntent(SectionFormUiIntent.GradeSelected(it)) } },
                modifier = Modifier.fillMaxWidth(),
            )
            GTextField(
                value = state.sectionName,
                onValueChange = { onIntent(SectionFormUiIntent.SectionNameChanged(it)) },
                label = stringResource(R.string.sections_form_name_label),
                modifier = Modifier.fillMaxWidth(),
                errorText = state.sectionNameError,
            )
            if (state.canDelete) {
                GButton(
                    text = stringResource(R.string.sections_form_delete_button),
                    onClick = { onIntent(SectionFormUiIntent.DeleteClicked) },
                    variant = GButtonVariant.DESTRUCTIVE,
                )
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
            style = GTextStyle.BODY_MEDIUM,
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
                sectionId = "section-1",
                grade = Grade.THIRD,
                sectionName = "A",
                canSave = true,
                canDelete = true,
            ),
            onIntent = {},
        )
    }
}
