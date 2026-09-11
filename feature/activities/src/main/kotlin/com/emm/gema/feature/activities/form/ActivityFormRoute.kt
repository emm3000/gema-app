package com.emm.gema.feature.activities.form

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
import com.emm.gema.feature.activities.R
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
    val messages: Map<ActivityFormMessage, String> = activityFormMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ActivityFormUiEffect.NavigateToActivityEvidence -> onActivityEvidence(effect.activityId)
                ActivityFormUiEffect.NavigateBack -> onBack()
                is ActivityFormUiEffect.ShowMessage -> message = messages.getValue(effect.message)
            }
        }
    }

    ActivityFormScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        dateErrorText = state.value.dateError?.let { messages.getValue(it) },
    )
}

@Composable
private fun activityFormMessages(): Map<ActivityFormMessage, String> = mapOf(
    ActivityFormMessage.OUTSIDE_PERIODS to stringResource(R.string.activity_form_message_outside_periods),
    ActivityFormMessage.SAVE_FAILED to stringResource(R.string.activity_form_message_save_failed),
)
