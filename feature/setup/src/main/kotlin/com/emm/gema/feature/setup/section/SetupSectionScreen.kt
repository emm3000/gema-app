package com.emm.gema.feature.setup.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.emm.gema.core.domain.section.Grade
import com.emm.gema.core.domain.section.label
import com.emm.gema.core.theme.GemaSpacing
import com.emm.gema.core.theme.GemaTheme
import com.emm.gema.core.ui.GBanner
import com.emm.gema.core.ui.GBannerActionStyle
import com.emm.gema.core.ui.GBannerTone
import com.emm.gema.core.ui.GButton
import com.emm.gema.core.ui.GChoiceChipOption
import com.emm.gema.core.ui.GChoiceChipRow
import com.emm.gema.core.ui.GScreen
import com.emm.gema.core.ui.GStepHeader
import com.emm.gema.core.ui.GText
import com.emm.gema.core.ui.GTextField
import com.emm.gema.core.ui.GTextStyle
import com.emm.gema.core.ui.GTopBar
import com.emm.gema.feature.setup.R

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
            GStepHeader(
                step = stringResource(R.string.setup_section_step_label),
                title = stringResource(R.string.setup_section_title),
                description = stringResource(R.string.setup_section_helper_text),
            )
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
                GText(text = stringResource(R.string.setup_section_grade_label), style = GTextStyle.LABEL_MEDIUM)
                GChoiceChipRow(
                    options = Grade.entries.map {
                        GChoiceChipOption(
                            value = it,
                            label = "${it.number}",
                            contentDescription = "Grado ${it.label()}",
                        )
                    },
                    selected = state.grade,
                    onSelect = { grade -> grade?.let { onIntent(SetupSectionUiIntent.GradeSelected(it)) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GTextField(
                value = state.sectionName,
                onValueChange = { onIntent(SetupSectionUiIntent.SectionNameChanged(it)) },
                label = stringResource(R.string.setup_section_name_label),
                modifier = Modifier.fillMaxWidth(),
                errorText = sectionNameError,
            )
            GBanner(
                text = stringResource(R.string.setup_section_areas_banner),
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.Info,
                actionText = stringResource(R.string.setup_section_areas_link),
                onActionClick = { onIntent(SetupSectionUiIntent.AreaSelectionClicked) },
                actionStyle = GBannerActionStyle.LINK,
            )
            GText(
                text = stringResource(R.string.setup_section_multigrade_caption),
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
