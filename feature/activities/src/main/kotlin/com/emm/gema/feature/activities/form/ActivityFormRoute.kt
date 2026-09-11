package com.emm.gema.feature.activities.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ActivityFormRoute(
    sectionId: String,
    activityId: String?,
    onActivityEvidence: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivityFormViewModel = koinViewModel { parametersOf(sectionId, activityId) }
    val state: State<ActivityFormUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ActivityFormUiEffect.NavigateToActivityEvidence -> onActivityEvidence(effect.activityId)
                ActivityFormUiEffect.NavigateBack -> onBack()
                is ActivityFormUiEffect.ShowMessage -> message = effect.text
            }
        }
    }

    ActivityFormScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
    )
}
