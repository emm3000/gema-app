package com.emm.gema.feature.setup.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.gema.feature.setup.SetupDraftStore
import com.emm.gema.feature.setup.year.SchoolYearDraft
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun SetupSectionRoute(
    onFinished: () -> Unit,
    onAreaSelection: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    draftStore: SetupDraftStore = koinInject(),
) {
    val draft: SchoolYearDraft = draftStore.take() ?: return
    val viewModel: SetupSectionViewModel = koinViewModel { parametersOf(draft) }
    val state: State<SetupSectionUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                SetupSectionUiEffect.NavigateToHome -> onFinished()
                is SetupSectionUiEffect.NavigateToSectionAreas -> onAreaSelection(effect.sectionId)
                SetupSectionUiEffect.NavigateBack -> onBack()
                is SetupSectionUiEffect.ShowMessage -> message = effect.text
            }
        }
    }

    SetupSectionScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}
