package com.emm.gema.feature.setup.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.core.domain.section.SectionId
import com.emm.gema.feature.setup.R
import com.emm.gema.feature.setup.SetupDraftStore
import com.emm.gema.feature.setup.year.SchoolYearDraft
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun SetupSectionRoute(
    onFinished: () -> Unit,
    onAreaSelection: (SectionId) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    draftStore: SetupDraftStore = koinInject(),
) {
    val draft: SchoolYearDraft = draftStore.take() ?: return
    val viewModel: SetupSectionViewModel = koinViewModel { parametersOf(draft) }
    val state: State<SetupSectionUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }
    val messages: Map<SetupSectionMessage, String> = setupSectionMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                SetupSectionUiEffect.NavigateToHome -> onFinished()
                is SetupSectionUiEffect.NavigateToSectionAreas -> onAreaSelection(effect.sectionId)
                SetupSectionUiEffect.NavigateBack -> onBack()
                is SetupSectionUiEffect.ShowMessage -> message = messages.getValue(effect.message)
            }
        }
    }

    SetupSectionScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        sectionNameError = state.value.sectionNameError?.let { messages.getValue(it) },
        message = message,
        onMessageDismissed = { message = null },
    )
}

@Composable
private fun setupSectionMessages(): Map<SetupSectionMessage, String> = mapOf(
    SetupSectionMessage.MISSING_NAME to stringResource(R.string.setup_section_error_missing_name),
    SetupSectionMessage.SAVE_FAILED to stringResource(R.string.setup_section_message_save_failed),
)
