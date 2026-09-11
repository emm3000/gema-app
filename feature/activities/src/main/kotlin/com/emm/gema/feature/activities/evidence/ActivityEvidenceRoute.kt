package com.emm.gema.feature.activities.evidence

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
fun ActivityEvidenceRoute(
    activityId: String,
    onActivityForm: (String, String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivityEvidenceViewModel = koinViewModel { parametersOf(activityId) }
    val state: State<ActivityEvidenceUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ActivityEvidenceUiEffect.NavigateToActivityForm ->
                    onActivityForm(effect.sectionId, effect.activityId)

                ActivityEvidenceUiEffect.NavigateBack -> onBack()
                is ActivityEvidenceUiEffect.ShowMessage -> message = effect.text
            }
        }
    }

    ActivityEvidenceScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
    )
}
