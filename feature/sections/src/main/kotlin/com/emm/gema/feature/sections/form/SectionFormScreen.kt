package com.emm.gema.feature.sections.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.domain.section.label

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
                title = if (state.sectionId == null) "Nueva sección" else "Editar sección",
                onBackClick = { onIntent(SectionFormUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Guardar",
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
                    actionText = "Entendido",
                    onActionClick = onMessageDismissed,
                )
            }
            GSegmentedPicker(
                options = Grade.entries.map {
                    GSegmentOption(value = it, label = it.label(), contentDescription = "Grado ${it.label()}")
                },
                selected = state.grade,
                onSelect = { grade -> grade?.let { onIntent(SectionFormUiIntent.GradeSelected(it)) } },
                modifier = Modifier.fillMaxWidth(),
            )
            GTextField(
                value = state.sectionName,
                onValueChange = { onIntent(SectionFormUiIntent.SectionNameChanged(it)) },
                label = "Nombre de la sección",
                modifier = Modifier.fillMaxWidth(),
                errorText = state.sectionNameError,
            )
            if (state.canDelete) {
                GButton(
                    text = "Eliminar sección",
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
        title = "¿Eliminar la sección?",
        confirmText = "Eliminar",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        dismissText = "Cancelar",
        isDestructive = true,
    ) {
        Text(
            text = "Se perderán ${confirmation.studentCount} estudiantes, " +
                "${confirmation.attendanceDayCount} días de asistencia y " +
                "${confirmation.periodLevelCount} niveles de logro.",
            style = MaterialTheme.typography.bodyMedium,
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
