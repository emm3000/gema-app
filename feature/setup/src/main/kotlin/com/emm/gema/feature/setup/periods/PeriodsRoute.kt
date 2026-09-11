package com.emm.gema.feature.setup.periods

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
import com.emm.gema.core.domain.schoolyear.SchoolYearId
import com.emm.gema.feature.setup.R
import com.emm.gema.feature.setup.resolve
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PeriodsRoute(
    schoolYearId: SchoolYearId,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PeriodsViewModel = koinViewModel { parametersOf(schoolYearId) }
    val state: State<PeriodsUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: String? by remember { mutableStateOf(null) }
    val messages: Map<PeriodsMessage, String> = periodsMessages()

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PeriodsUiEffect.NavigateBack -> onBack()
                is PeriodsUiEffect.ShowMessage -> message = messages.getValue(effect.message)
            }
        }
    }

    PeriodsScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message,
        onMessageDismissed = { message = null },
        overlapErrorText = state.value.overlapError?.resolve(),
    )
}

@Composable
private fun periodsMessages(): Map<PeriodsMessage, String> = mapOf(
    PeriodsMessage.SAVE_FAILED to stringResource(R.string.setup_periods_message_save_failed),
)
