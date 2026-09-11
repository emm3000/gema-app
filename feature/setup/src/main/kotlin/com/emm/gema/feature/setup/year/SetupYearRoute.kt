package com.emm.gema.feature.setup.year

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import com.emm.gema.feature.setup.SetupDraftStore
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SetupYearRoute(
    onDraftReady: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetupYearViewModel = koinViewModel(),
    draftStore: SetupDraftStore = koinInject(),
) {
    val state: State<SetupYearUiState> = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SetupYearUiEffect.NavigateToSetupSection -> {
                    draftStore.put(effect.draft)
                    onDraftReady()
                }
                SetupYearUiEffect.NavigateBack -> onBack()
            }
        }
    }

    SetupYearScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
