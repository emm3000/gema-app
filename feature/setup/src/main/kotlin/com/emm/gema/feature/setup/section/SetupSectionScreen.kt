package com.emm.gema.feature.setup.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GSegmentOption
import com.emm.gema.core.ui.GSegmentedPicker
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.core.domain.section.label

@Composable
fun SetupSectionScreen(
    state: SetupSectionUiState,
    onIntent: (SetupSectionUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    sectionNameError: String? = null,
    message: String? = null,
    onMessageDismissed: () -> Unit = {},
) {
    GScreen(
        topBar = {
            GTopBar(
                title = "Tu primera sección",
                subtitle = "Paso 2 de 2",
                onBackClick = { onIntent(SetupSectionUiIntent.BackClicked) },
            )
        },
        modifier = modifier,
        bottomAction = {
            GButton(
                text = "Terminar",
                onClick = { onIntent(SetupSectionUiIntent.FinishClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canFinish,
                isBusy = state.isSaving,
            )
        },
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = GemaSpacing.screenGutter),
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
                onSelect = { grade -> grade?.let { onIntent(SetupSectionUiIntent.GradeSelected(it)) } },
                modifier = Modifier.fillMaxWidth(),
            )
            GTextField(
                value = state.sectionName,
                onValueChange = { onIntent(SetupSectionUiIntent.SectionNameChanged(it)) },
                label = "Nombre de la sección",
                modifier = Modifier.fillMaxWidth(),
                errorText = sectionNameError,
            )
            GText(
                text = "Empiezas con todas las áreas activas. Puedes apagar las que no dictas.",
                style = GTextStyle.BODY_MEDIUM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GButton(
                text = "No dicto todas las áreas",
                onClick = { onIntent(SetupSectionUiIntent.AreaSelectionClicked) },
                variant = GButtonVariant.TEXT,
                enabled = state.canFinish && !state.isSaving,
            )
            GText(
                text = "Si enseñas en aula multigrado, crea una sección por grado.",
                style = GTextStyle.BODY_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SetupSectionScreenPreview() {
    GemaTheme {
        SetupSectionScreen(
            state = SetupSectionUiState(grade = Grade.THIRD, sectionName = "A", canFinish = true),
            onIntent = {},
        )
    }
}
