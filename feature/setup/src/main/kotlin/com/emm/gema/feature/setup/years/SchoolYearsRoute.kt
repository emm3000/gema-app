package com.emm.gema.feature.setup.years

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SchoolYearsRoute(
    onPeriods: (String) -> Unit,
    onAddYear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SchoolYearsViewModel = koinViewModel(),
) {
    val state: State<SchoolYearsUiState> = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is SchoolYearsUiEffect.NavigateToPeriods -> onPeriods(effect.id)
                SchoolYearsUiEffect.NavigateToSetupYear -> onAddYear()
                SchoolYearsUiEffect.NavigateBack -> onBack()
            }
        }
    }

    SchoolYearsScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
