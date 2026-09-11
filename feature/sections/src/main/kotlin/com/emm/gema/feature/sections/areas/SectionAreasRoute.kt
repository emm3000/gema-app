package com.emm.gema.feature.sections.areas

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
import com.emm.gema.feature.sections.R
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SectionAreasRoute(
    sectionId: SectionId,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SectionAreasViewModel = koinViewModel { parametersOf(sectionId) }
    val state: State<SectionAreasUiState> = viewModel.state.collectAsStateWithLifecycle()
    var message: SectionAreasMessage? by remember { mutableStateOf(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                SectionAreasUiEffect.NavigateBack -> onBack()
                is SectionAreasUiEffect.ShowMessage -> message = effect.message
            }
        }
    }

    SectionAreasScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        message = message?.let { stringResource(sectionAreasMessageRes(it)) },
        onMessageDismissed = { message = null },
    )
}

private fun sectionAreasMessageRes(message: SectionAreasMessage): Int = when (message) {
    SectionAreasMessage.TOGGLE_FAILED -> R.string.sections_areas_message_toggle_failed
}
