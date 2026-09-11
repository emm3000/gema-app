package com.emm.gema.feature.activities.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ActivitiesRoute(
    sectionId: String,
    onActivityEvidence: (String) -> Unit,
    onActivityForm: (String, String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivitiesViewModel = koinViewModel { parametersOf(sectionId) }
    val state: State<ActivitiesUiState> = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ActivitiesUiEffect.NavigateToActivityEvidence -> onActivityEvidence(effect.activityId)
                is ActivitiesUiEffect.NavigateToActivityForm -> onActivityForm(effect.sectionId, effect.activityId)
                ActivitiesUiEffect.NavigateBack -> onBack()
            }
        }
    }

    ActivitiesScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
