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
                title = "",
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
            Header()
            if (message != null) {
                GBanner(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    tone = GBannerTone.ERROR,
                    actionText = "Entendido",
                    onActionClick = onMessageDismissed,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(GemaSpacing.small)) {
                GText(text = "Grado", style = GTextStyle.LABEL_MEDIUM)
                GSegmentedPicker(
                    options = Grade.entries.map {
                        GSegmentOption(value = it, label = it.label(), contentDescription = "Grado ${it.label()}")
                    },
                    selected = state.grade,
                    onSelect = { grade -> grade?.let { onIntent(SetupSectionUiIntent.GradeSelected(it)) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GTextField(
                value = state.sectionName,
                onValueChange = { onIntent(SetupSectionUiIntent.SectionNameChanged(it)) },
                label = "Nombre de la sección",
                modifier = Modifier.fillMaxWidth(),
                errorText = sectionNameError,
            )
            GBanner(
                text = "Todas las áreas quedan activas.",
                modifier = Modifier.fillMaxWidth(),
                actionText = "No dicto todas las áreas",
                onActionClick = { onIntent(SetupSectionUiIntent.AreaSelectionClicked) },
            )
            GText(
                text = "Si enseñas en aula multigrado, crea una sección por grado.",
                style = GTextStyle.BODY_SMALL,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GemaSpacing.extraSmall)) {
        GText(text = "Paso 2 de 2", style = GTextStyle.LABEL_MEDIUM)
        GText(text = "Tu primera sección", style = GTextStyle.TITLE_MEDIUM)
        GText(
            text = "Ya casi. Después puedes crear todas las secciones que dictes.",
            style = GTextStyle.BODY_MEDIUM,
        )
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
